package br.com.overload.usuarioservice.controller;

import br.com.overload.usuarioservice.dto.LoginRequestDTO;
import br.com.overload.usuarioservice.dto.LoginResponseDTO;
import br.com.overload.usuarioservice.service.AutenticacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping
    public ResponseEntity<LoginResponseDTO> autenticarUsuario(@Valid @RequestBody LoginRequestDTO loginRequestDTO){
        LoginResponseDTO loginResponseDTO = autenticacaoService.autenticacaoLogin(loginRequestDTO);

        return new ResponseEntity<>(loginResponseDTO,HttpStatus.OK);
    }
}
