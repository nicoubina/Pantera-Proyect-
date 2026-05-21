package ar.edu.huergo.ioliveto.panterfitness.repository;

import ar.edu.huergo.ioliveto.panterfitness.entity.ClaseGimnasio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaseGimnasioRepository extends JpaRepository<ClaseGimnasio, Long> {

	List<ClaseGimnasio> findByActivaTrueOrderByNombreAsc();

	List<ClaseGimnasio> findByProfesorIdAndActivaTrueOrderByNombreAsc(Long profesorId);
}
