package ar.edu.huergo.ioliveto.panterfitness.repository;

import ar.edu.huergo.ioliveto.panterfitness.entity.Notificacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

	List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
}
