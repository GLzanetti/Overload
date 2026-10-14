package br.com.overload.treinoservice.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "treino", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "nome"}))
public class Treino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(nullable = false)
    private Instant criadoEm;

    public Treino() {
    }

    public Treino(String nome, Long usuarioId) {
        this.nome = nome;
        this.usuarioId = usuarioId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    @PrePersist
    protected void prePersist(){
        this.criadoEm = Instant.now();
    }
}
