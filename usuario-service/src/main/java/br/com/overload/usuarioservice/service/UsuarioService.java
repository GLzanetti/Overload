package br.com.overload.usuarioservice.service;

import br.com.overload.usuarioservice.dto.UsuarioCadastroDTO;
import br.com.overload.usuarioservice.exception.EmailJaCadastradoException;
import br.com.overload.usuarioservice.model.Usuario;
import br.com.overload.usuarioservice.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService{

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private void validacaoDeEmailDisponivel(String email){
        if(usuarioRepository.existsByEmail(email)){
            throw new EmailJaCadastradoException();
        }
    }

    public Usuario criarUsuario(UsuarioCadastroDTO dto){

        validacaoDeEmailDisponivel(dto.getEmail());

        String senhaHash = passwordEncoder.encode(dto.getSenha());
        Usuario usuario = new Usuario(dto.getNome(), dto.getEmail(), senhaHash);

        return usuarioRepository.save(usuario);
    }


}
