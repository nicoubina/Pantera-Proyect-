package com.sistema.panterafitness.repository;

import com.sistema.panterafitness.entity.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByEmail(String email);

	Optional<Usuario> findByQrSimulado(String qrSimulado);

	boolean existsByEmail(String email);
}
