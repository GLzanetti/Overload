package br.com.overload.usuarioservice.exception;

public class EmailJaCadastradoException extends RuntimeException{

    public EmailJaCadastradoException() {
        super("Já existe um usuário cadastrado com esse email.");
    }


}
