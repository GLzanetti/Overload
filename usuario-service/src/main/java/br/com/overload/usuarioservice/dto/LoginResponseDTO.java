package br.com.overload.usuarioservice.dto;

public class LoginResponseDTO {

    private String nome;
    private String accessToken;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String nome, String accessToken) {
        this.nome = nome;
        this.accessToken = accessToken;
    }

    public String getNome() {
        return nome;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
