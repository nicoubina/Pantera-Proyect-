package ar.edu.huergo.ioliveto.panterfitness.repository;

import ar.edu.huergo.ioliveto.panterfitness.entity.Reserva;
import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoReserva;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

	List<Reserva> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

	List<Reserva> findByHorarioClaseIdOrderByFechaCreacionDesc(Long horarioClaseId);

	List<Reserva> findByUsuarioIdAndEstadoReservaIn(Long usuarioId, Collection<EstadoReserva> estados);

	Optional<Reserva> findByUsuarioIdAndHorarioClaseIdAndEstadoReserva(
			Long usuarioId,
			Long horarioClaseId,
			EstadoReserva estadoReserva
	);

	boolean existsByUsuarioIdAndHorarioClaseIdAndEstadoReservaIn(
			Long usuarioId,
			Long horarioClaseId,
			Collection<EstadoReserva> estados
	);

	long countByHorarioClaseIdAndEstadoReservaIn(Long horarioClaseId, Collection<EstadoReserva> estados);

	@Query("""
			select r
			from Reserva r
			where r.horarioClase.claseGimnasio.profesor.id = :profesorId
			order by r.fechaCreacion desc
			""")
	List<Reserva> findByProfesorId(@Param("profesorId") Long profesorId);
}
