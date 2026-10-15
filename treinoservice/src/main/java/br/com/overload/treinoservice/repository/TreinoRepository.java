package br.com.overload.treinoservice.repository;

import br.com.overload.treinoservice.model.Treino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreinoRepository extends JpaRepository<Treino, Long> {

    Optional<Treino> findByUsuarioIdAndNome(Long usuarioId, String nome);
    Optional<Treino> findByIdAndUsuarioIdAndAtivoTrue(Long id, Long usuarioId);
    List<Treino> findByUsuarioIdAndAtivoTrue(Long usuarioId);

}
