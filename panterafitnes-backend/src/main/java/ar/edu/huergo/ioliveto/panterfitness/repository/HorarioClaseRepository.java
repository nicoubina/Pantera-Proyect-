package ar.edu.huergo.ioliveto.panterfitness.repository;

import ar.edu.huergo.ioliveto.panterfitness.entity.HorarioClase;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HorarioClaseRepository extends JpaRepository<HorarioClase, Long> {

	List<HorarioClase> findByActivaTrueOrderByFechaAscHoraInicioAsc();

	List<HorarioClase> findByActivaTrueAndFechaBetweenOrderByFechaAscHoraInicioAsc(LocalDate desde, LocalDate hasta);

	@Query("""
			select h
			from HorarioClase h
			where h.activa = true
			and h.claseGimnasio.profesor.id = :profesorId
			order by h.fecha asc, h.horaInicio asc
			""")
	List<HorarioClase> findActivosByProfesorId(@Param("profesorId") Long profesorId);
}
