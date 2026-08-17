package br.com.overload.usuarioservice.exception;

import br.com.overload.usuarioservice.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final String servico;

    public GlobalExceptionHandler(@Value("${spring.application.name}") String servico) {
        this.servico = servico;
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErrorResponseDTO> tratamentoEmailJaCadastradoException(EmailJaCadastradoException e, HttpServletRequest request){

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(409, e.getMessage(), servico, Instant.now(), request.getRequestURI(), "EMAIL_JA_CADASTRADO");

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.CONFLICT);
    }
}
