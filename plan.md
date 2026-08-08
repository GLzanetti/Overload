# Plano de Desenvolvimento — Overload

> Este documento é um roteiro de trabalho, não uma receita fechada. Alguns pontos aqui trazem opções em vez de decisões prontas — de propósito, porque são decisões que valem a pena ser discutidas e tomadas por você ao longo do caminho, não herdadas de um plano gerado automaticamente.

## 0. Identidade do Projeto

- **Nome**: Overload
- **Descrição (GitHub)**: Overload — aplicação de monitoramento de evolução em treinos, construída com arquitetura de microsserviços (Java + Spring Boot). Cada domínio (Usuário, Treinos, Métricas) possui API e banco de dados próprios, com bancos relacionais e NoSQL, containerização via Docker e deploy na AWS (EC2).
- **Licença**: MIT

---

## 1. Estrutura de Pastas

Antes de definir a estrutura, existe uma decisão de fundo: **monorepo** (todos os serviços em um único repositório Git) ou **multi-repo** (um repositório por serviço).

| Abordagem | Vantagens | Desvantagens |
|---|---|---|
| **Monorepo** | Mais fácil de navegar e revisar o projeto como um todo (bom para portfólio); um único `docker-compose.yml` na raiz orquestra tudo; menos fricção no início do projeto | Menos fiel ao "mundo real" de microsserviços, onde cada serviço costuma ter seu próprio ciclo de vida/deploy/repo |
| **Multi-repo** | Mais fiel à prática real de mercado; reforça a ideia de que cada serviço é independente | Mais fricção para gerenciar (você precisa versionar `docker-compose` em algum lugar separado, ou duplicar); mais complexo de mostrar em portfólio de forma coesa |

**Sugestão para essa fase do projeto:** monorepo. Isso não invalida o aprendizado de microsserviços — o isolamento continua existindo a nível de código, banco de dados e processo (cada serviço roda em seu próprio container, com seu próprio banco). O monorepo só facilita a organização enquanto o projeto ainda está em construção. Migrar para multi-repo depois, se quiser, é trivial (é só separar as pastas em repositórios novos).

Estrutura sugerida (monorepo):

```
overload/
├── docker-compose.yml              # orquestra todos os serviços + bancos localmente
├── .env.example                    # variáveis de ambiente (sem segredos reais)
├── LICENSE                         # MIT
├── plan.md
├── projeto-webapp-academia.md
├── agentementor.md
│
├── gateway-service/
│   ├── src/main/java/.../gateway/
│   │   ├── security/           # filtro de validação de JWT
│   │   ├── router/             # lógica de repasse/roteamento para os serviços internos
│   │   └── exception/
│   ├── Dockerfile
│   └── pom.xml
│
├── usuario-service/
│   ├── src/main/java/.../usuario/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── domain/ (entidades)
│   │   ├── dto/
│   │   ├── security/          # JWT, filtros de autenticação
│   │   └── exception/         # handler centralizado
│   ├── src/main/resources/application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── treinos-service/
│   ├── src/main/java/.../treinos/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── domain/
│   │   ├── dto/
│   │   ├── client/            # cliente HTTP para chamar usuario-service (validar usuário)
│   │   └── exception/
│   ├── Dockerfile
│   └── pom.xml
│
├── metricas-service/
│   ├── src/main/java/.../metricas/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/        # repositório NoSQL (ex: MongoRepository)
│   │   ├── domain/             # documentos NoSQL
│   │   ├── dto/
│   │   ├── client/            # cliente HTTP para treinos-service (validar exercício)
│   │   └── exception/
│   ├── Dockerfile
│   └── pom.xml
│
└── docs/
    └── (diagramas de arquitetura, coleções Postman/Swagger exportado, etc.)
```

Um ponto que vale já ir se acostumando: cada `*-service` deve conseguir subir e ser testado **isoladamente**, mesmo estando no mesmo repositório. Se em algum momento um serviço só "funciona" quando outro está rodando junto (fora chamadas HTTP explícitas), isso é sinal de acoplamento indevido — vale revisitar a separação de responsabilidades quando isso acontecer.

---

## 2. Ordem de Implementação das APIs

A ordem não é arbitrária — ela segue a cadeia de dependência real entre os domínios.

### 2.1 — Usuário / Autenticação (primeiro)

É a base de tudo. Treinos pertencem a um usuário, e a validação de quem está autenticado passa pelo `gateway-service` antes de chegar em qualquer outro serviço (ver decisão de JWT abaixo).

Escopo mínimo sugerido para essa primeira API:
- Cadastro de usuário
- Login (emissão de JWT, assinado com a chave secreta compartilhada com o Gateway)
- Endpoint de perfil (`GET /me`, autenticado)

**Decisão de JWT — fechada (fase 1):** validação **centralizada**, feita por um `gateway-service` implementado manualmente (Spring MVC comum, sem Spring Cloud Gateway nessa fase). Ele é o único ponto da aplicação com dependência de Spring Security relacionada a JWT — `treinos-service` e `metricas-service` não validam token, apenas confiam na identidade repassada pelo Gateway. `usuario-service` continua sendo quem emite o token no login.

A migração para Spring Cloud Gateway (WebFlux/reativo) fica planejada para a Fase 2 do projeto — ver seção 7.

#### Desenho consolidado do `gateway-service`

Mecanismo fechado após discussão — resumo de como as peças se encaixam:

1. **Roteamento**: um `Map<String, String>` relacionando prefixo de path → endereço interno do serviço (ex: `/usuarios -> http://usuario-service:8080`). Escolhido em vez de `if/else` porque é mais alinhado ao Open/Closed Principle — adicionar um serviço novo vira "adicionar uma entrada no Map", sem alterar a lógica de roteamento já existente.
2. **Validação do JWT**: um `OncePerRequestFilter` no Gateway intercepta toda requisição, extrai o header `Authorization`, valida a assinatura/expiração do token e decodifica o `sub` (ID do usuário). Se inválido/ausente, a requisição é cortada ali mesmo com 401, sem nunca chegar ao serviço de destino.
3. **Propagação da identidade**: o Gateway **não repassa o JWT original** para os serviços internos (eles não sabem lidar com token). Em vez disso, adiciona um **header HTTP customizado** (`X-User-Id`) com o ID já extraído, e repassa a requisição pro serviço de destino com esse header. Do lado do serviço de destino, isso é lido com um simples `@RequestHeader("X-User-Id")` no controller — nenhuma dependência de Spring Security ou JWT necessária ali.
4. **Fronteira de confiança**: esse mecanismo só é seguro porque `usuario-service`, `treinos-service` e `metricas-service` **não ficam expostos publicamente** — apenas o Gateway tem porta aberta para fora (reforça o ponto já registrado na seção de AWS sobre Security Groups). Chamadas **serviço → serviço** (ex: `metricas-service` validando um exercício no `treinos-service`) acontecem direto pela rede interna do Docker, sem passar pelo Gateway — não é tráfego de borda, então não precisa da validação de JWT novamente.

### 2.2 — Treinos (segundo)

Depende de Usuário existir (todo treino pertence a um usuário). Não depende de Métricas.

Escopo mínimo:
- CRUD de treinos (nome, usuário dono)
- CRUD de exercícios dentro de um treino
- Validação: o treino/exercício só pode ser manipulado pelo usuário dono

### 2.3 — Métricas / Progresso (terceiro)

É o serviço com maior dependência dos outros dois: precisa saber que o exercício existe (chamando Treinos) e para qual usuário está registrando dados (autenticação).

Escopo mínimo:
- Registro de carga/repetições por exercício e data
- Endpoint de "evolução" (busca o histórico e calcula em tempo real, como já decidido no documento do projeto)
- Esse é o momento natural para tomar a decisão do banco NoSQL, já que é aqui que ela importa de fato

### Por que essa ordem e não outra?

Um erro comum é começar pela parte "mais interessante" (no caso, provavelmente Métricas, por causa dos gráficos e cálculos). O problema é que, sem Usuário e Treinos prontos — nem que seja em versão mínima — você não tem como testar Métricas de forma realista, porque ela depende de dados que vêm dos outros dois serviços. Construir de baixo para cima na cadeia de dependências evita retrabalho e permite testar cada serviço com dados reais (não mockados) assim que ele nasce.

---

## 3. Pontos Sugeridos de Versionamento com Git

Alguns marcos naturais para commits/branches — pensados para contar uma história legível no histórico do repositório (bom tanto para você acompanhar seu próprio progresso quanto para quem for olhar o portfólio depois):

1. **Setup inicial**: estrutura de pastas vazia, `docker-compose.yml` esqueleto, `.gitignore` (não esquecer de ignorar `target/`, `.env`, arquivos de IDE) → primeiro commit.
2. **Usuário-service**: sugestão de branch `feature/usuario-cadastro-login`, com commits pequenos e atômicos (ex: "cria entidade Usuario", "implementa endpoint de cadastro", "adiciona geração de JWT no login") em vez de um único commit gigante no final.
3. **Gateway-service** (proxy manual + validação de JWT): branch própria, ex: `feature/gateway-jwt`. Vale registrar no commit (ou em um `ADR` dentro de `docs/`) o porquê da escolha de centralizar a validação aqui em vez de descentralizar — é uma decisão estrutural que merece ficar documentada.
4. **Treinos-service**: mesma lógica de commits pequenos; branch própria, ex: `feature/treinos-crud`.
5. **Integração Treinos → Usuário** (via Gateway): quando as chamadas passarem a fluir pelo Gateway com repasse de identidade (`X-User-Id`), isso merece commit isolado (é um ponto sensível de acoplamento entre serviços).
6. **Métricas-service**: branch própria, incluindo a decisão do banco NoSQL documentada (ex: no commit ou em um `ADR` dentro de `docs/`).
7. **Docker Compose funcional** (todos os serviços + Gateway + bancos subindo juntos): marco importante, vale uma tag no Git (ex: `v0.1-local-completo`).
8. **Primeiro deploy na AWS**: outro bom ponto para tag (ex: `v0.1-deploy-aws`).

Uma prática que vale adotar desde já, já que você vai lidar com múltiplos serviços: **Conventional Commits** (`feat:`, `fix:`, `refactor:`, `docs:`, etc.). Isso ajuda bastante quando o histórico cresce e você precisa entender rapidamente o que cada commit fez, especialmente entre serviços diferentes.

---

## 4. Pontos de Atenção — Docker / Docker Compose

- Cada serviço deve ter seu **próprio `Dockerfile`**, e o `docker-compose.yml` na raiz orquestra serviços + bancos juntos. Vale usar multi-stage build no Dockerfile (build com Maven em uma stage, imagem final só com o `.jar` e um JRE enxuto) — isso reduz bastante o tamanho da imagem final.
- Cada banco de dados (relacional dos serviços de Usuário/Treinos, e o NoSQL de Métricas) deve rodar em seu **próprio container**, com **volume próprio**, para persistir dados entre reinicializações.
- Vale definir uma rede Docker própria no `docker-compose.yml` para que os serviços se enxerguem pelo nome do container (ex: `http://usuario-service:8080`) em vez de `localhost` — isso já simula, mesmo que localmente, como a comunicação vai funcionar num ambiente real.
- `depends_on` no `docker-compose.yml` controla ordem de subida dos containers, mas **não** garante que a aplicação lá dentro já esteja pronta para receber requisições (só garante que o container começou a subir). Isso é um erro comum — vale ter isso em mente quando for integrar os serviços, especialmente o healthcheck dos bancos antes das aplicações dependerem deles.
- Variáveis sensíveis (senha de banco, secret do JWT) não devem ir hardcoded no `docker-compose.yml` nem no código — usar `.env` (fora do Git) e `application.yml` referenciando variáveis de ambiente.

---

## 5. Pontos de Atenção — Deploy na AWS

- Como o plano já prevê EC2, vale decidir cedo: uma única instância EC2 rodando todos os serviços via Docker Compose (mais simples, bom para essa fase) ou uma instância por serviço (mais próximo de produção real, mas mais caro e mais complexo de gerenciar nesse estágio). Dado que o objetivo agora é aprendizado e portfólio, uma instância única com Docker Compose tende a ser o ponto de partida mais razoável — mensageria e escalonamento independente por serviço já foram deixados para fases futuras no próprio planejamento do projeto, então faz sentido a infraestrutura acompanhar esse mesmo ritmo.
- Grupos de segurança (Security Groups): abrir só as portas estritamente necessárias (ex: 22 para SSH restrito ao seu IP, portas das APIs se forem expostas diretamente, ou só a porta de um eventual API Gateway/reverse proxy).
- Vale considerar um **reverse proxy** (Nginx, por exemplo) na frente dos serviços na EC2, tanto para expor todos sob portas padrão (80/443) quanto como candidato natural, no futuro, a assumir o papel de Gateway (inclusive para a decisão pendente de validação centralizada de JWT).
- Já que você tem a certificação AWS Cloud Practitioner e outro projeto usando ALB/ASG/multi-AZ: nessa fase, isso é mais complexidade do que o projeto precisa (o foco agora é aprender microsserviços e bancos variados, não resiliência de infraestrutura). Mas vale deixar anotado como possível evolução futura do projeto, assim como a mensageria.
- Não esquecer de gerenciamento de custos: instância EC2 dentro do free tier (se ainda disponível na sua conta), e lembrar de desligar a instância quando não estiver testando/demonstrando, para não gerar custo desnecessário.

---

## 6. Bancos de Dados por Serviço

| Serviço | Banco | Status |
|---|---|---|
| `usuario-service` | PostgreSQL | Fechado |
| `treinos-service` | PostgreSQL | Fechado |
| `metricas-service` | MongoDB | Fechado |

PostgreSQL escolhido conscientemente em vez do MySQL/MariaDB já dominado pelo autor, priorizando aprendizado de uma tecnologia nova (ganho de portfólio) — sem trade-off técnico relevante para o domínio do projeto. Os dois serviços relacionais compartilham o mesmo motor de banco (Postgres), evitando complexidade de infraestrutura desnecessária.

MongoDB confirmado como o NoSQL de `metricas-service`, alinhado ao candidato natural já apontado no `projeto-webapp-academia.md` (dados de série temporal — "nesta data, este exercício, esta carga" — se encaixam bem no modelo de documento).

A **modelagem detalhada** (tabelas/documentos, campos) de cada banco ainda está em aberto — ver seção 8.

---

## 7. Padrão de Tratamento de Exceções

Cada serviço mantém suas próprias exceptions de domínio e seu próprio `@ControllerAdvice` (tratamento local, sem lib compartilhada entre serviços — evita o mesmo tipo de acoplamento já discutido na decisão de JWT). O que é compartilhado é o **contrato do formato de erro** (por convenção/documentação, não por código): cada serviço define sua própria cópia de um `ErrorResponseDTO` com os mesmos campos.

Campos do `ErrorResponseDTO` (padrão entre os três serviços):

| Campo | Descrição |
|---|---|
| `status` | Código HTTP do erro (404, 400, 409, etc.) |
| `mensagem` | Descrição legível do erro |
| `servico` | Nome do serviço de origem (essencial em sistema distribuído, para localizar logs) |
| `timestamp` | Momento em que o erro ocorreu (útil para cruzar com logs em produção) |
| `path` | Endpoint chamado quando o erro ocorreu |
| `codigoErro` *(opcional)* | Código interno (ex: `USUARIO_NAO_ENCONTRADO`), separado da mensagem em texto — útil se o frontend precisar tratar tipos de erro programaticamente no futuro |

**Pendente para quando implementarmos as chamadas entre serviços:** definir se um erro recebido de outro serviço (ex: `treinos-service` chamando `usuario-service`) é repassado como está para o cliente final, ou traduzido/embrulhado pelo serviço que fez a chamada.

---

## 8. Decisões em Aberto (retomadas do documento do projeto)

Essas decisões não foram tomadas aqui de propósito — o ideal é discuti-las quando chegarmos no ponto certo da implementação, com trade-offs na mesa:

- Modelagem detalhada de cada banco de dados (tabelas do Postgres em `usuario-service`/`treinos-service`, formato dos documentos no MongoDB de `metricas-service`) — próximo passo de discussão
- Se erros recebidos de chamadas entre serviços são repassados como estão ou traduzidos/embrulhados (ver seção 7)

---

## 9. Fase 2 — Evoluções Futuras (pós-versão inicial)

Conceitos identificados como "avançados demais" para a primeira versão do projeto, deliberadamente adiados para depois que a base (Usuário, Treinos, Métricas e Gateway manual) estiver funcionando de ponta a ponta. A ideia é introduzir cada um desses tópicos isoladamente, com mais contexto acumulado do projeto, em vez de acumular complexidade nova em cima de uma base ainda instável.

- **Spring Cloud Gateway (WebFlux/reativo)**: migração do `gateway-service` manual (Spring MVC) para o Spring Cloud Gateway, introduzindo programação reativa (`Mono`/`Flux`, non-blocking) como novo conceito de estudo. Nesse momento vale reavaliar também a validação de JWT usando `ReactiveJwtDecoder`.
- **Mensageria (Kafka ou RabbitMQ)**: já prevista no documento original do projeto para registro de eventos de progresso — por exemplo, `treinos-service` publicando um evento quando um exercício é criado/alterado, e `metricas-service` consumindo esse evento em vez de (ou além de) ser chamado via REST síncrono. Essa mudança introduz comunicação assíncrona entre serviços, um contraponto interessante à comunicação síncrona via REST usada na fase 1. Kafka x RabbitMQ é uma decisão a ser discutida quando chegarmos nessa fase (trade-offs: Kafka é mais voltado a streaming/alto volume e retenção de eventos; RabbitMQ é mais simples de operar e mais focado em filas/mensageria tradicional).
- **Resiliência de infraestrutura na AWS** (ALB, Auto Scaling Group, múltiplas Availability Zones): natural próximo passo depois do deploy inicial em uma única EC2, aproveitando a experiência que você já tem de outro projeto de portfólio.

---

## Próximo Passo Sugerido

Com o plano no papel, o próximo passo natural é começar pelo `usuario-service`: estrutura mínima do projeto Spring Boot (dependências, `application.yml`, entidade `Usuario`) antes de partir para cadastro/login. Quando quiser começar essa parte, me chama.
