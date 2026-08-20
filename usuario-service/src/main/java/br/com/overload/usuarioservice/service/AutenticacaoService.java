package br.com.overload.usuarioservice.service;

import br.com.overload.usuarioservice.dto.LoginRequestDTO;
import br.com.overload.usuarioservice.dto.LoginResponseDTO;
import br.com.overload.usuarioservice.exception.CredenciaisInvalidasException;
import br.com.overload.usuarioservice.model.Usuario;
import br.com.overload.usuarioservice.repository.UsuarioRepository;
import br.com.overload.usuarioservice.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AutenticacaoService(UsuarioRepository usuarioRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponseDTO autenticacaoLogin(LoginRequestDTO loginRequestDTO){
        Usuario usuario = usuarioRepository.findByEmailAndAtivoTrue(loginRequestDTO.getEmail()).orElseThrow(CredenciaisInvalidasException::new);

        boolean senhaCorreta = passwordEncoder.matches(loginRequestDTO.getSenha(), usuario.getSenhaHash());

        if(!senhaCorreta){
            throw new CredenciaisInvalidasException();
        }

        String token = jwtService.gerarToken(usuario.getId());

        return new LoginResponseDTO(usuario.getNome(), token);
    }
}
