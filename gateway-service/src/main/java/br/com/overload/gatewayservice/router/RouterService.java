package br.com.overload.gatewayservice.router;

import br.com.overload.gatewayservice.config.GatewayProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RouterService {

    private final GatewayProperties gatewayProperties;

    public RouterService(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    public ResponseEntity<byte[]> rotear(String metodo, String path, byte[] corpo){
        String[] partes = path.split("/");
        String prefixo = partes[2];
        String enderecoBase = gatewayProperties.getRotas().get(prefixo);
        String urlDestino = enderecoBase + path;

        RestClient restClient = RestClient.create();

        ResponseEntity<byte[]> resposta = restClient
                .method(HttpMethod.valueOf(metodo))
                .uri(urlDestino).body(corpo)
                .retrieve()
                .toEntity(byte[].class);

        return resposta;
    }
}