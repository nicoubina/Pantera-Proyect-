package com.sistema.panterafitness.repository;

import com.sistema.panterafitness.entity.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select e from Usuario e where e.id = :id")
  java.util.Optional<Usuario> findLockedById(
      @org.springframework.data.repository.query.Param("id") Long id);

  Optional<Usuario> findByEmail(String email);

  Optional<Usuario> findByQrSimulado(String qrSimulado);

  boolean existsByEmail(String email);
}
