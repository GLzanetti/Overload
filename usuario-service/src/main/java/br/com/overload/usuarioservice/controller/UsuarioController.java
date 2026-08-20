package br.com.overload.usuarioservice.controller;

import br.com.overload.usuarioservice.dto.UsuarioCadastroDTO;
import br.com.overload.usuarioservice.dto.UsuarioResponseDTO;
import br.com.overload.usuarioservice.model.Usuario;
import br.com.overload.usuarioservice.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(@Valid @RequestBody UsuarioCadastroDTO usuarioCadastroDTO){
        Usuario usuario = usuarioService.criarUsuario(usuarioCadastroDTO);
        UsuarioResponseDTO usuarioResponseDTO = UsuarioResponseDTO.fromEntity(usuario);

        return new ResponseEntity<>(usuarioResponseDTO,HttpStatus.CREATED);
    }

}
