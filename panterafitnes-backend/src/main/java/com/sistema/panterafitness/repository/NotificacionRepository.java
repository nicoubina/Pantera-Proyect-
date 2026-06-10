package com.sistema.panterafitness.repository;

import com.sistema.panterafitness.entity.Notificacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

	List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
}
