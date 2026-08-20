package br.com.overload.usuarioservice.exception;

public class CredenciaisInvalidasException extends RuntimeException{

    public CredenciaisInvalidasException() {
        super("Credenciais inválidas.");
    }
}
