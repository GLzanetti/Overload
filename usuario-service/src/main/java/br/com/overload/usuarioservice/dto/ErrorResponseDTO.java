package br.com.overload.usuarioservice.dto;

import java.time.Instant;

public class ErrorResponseDTO {

    private int status;
    private String mensagem;
    private String servico;
    private Instant timestamp;
    private String path;
    private String codigoErro;

    public ErrorResponseDTO() {
    }

    public ErrorResponseDTO(int status, String mensagem, String servico, Instant timestamp, String path, String codigoErro) {
        this.status = status;
        this.mensagem = mensagem;
        this.servico = servico;
        this.timestamp = timestamp;
        this.path = path;
        this.codigoErro = codigoErro;
    }

    public int getStatus() {
        return status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getServico() {
        return servico;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }

    public String getCodigoErro() {
        return codigoErro;
    }
}
