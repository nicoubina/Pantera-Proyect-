package com.sistema.panterafitness.repository;

import com.sistema.panterafitness.entity.ClaseGimnasio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaseGimnasioRepository extends JpaRepository<ClaseGimnasio, Long> {
	java.util.Optional<ClaseGimnasio> findFirstByNombreAndProfesorIdAndSectorOrderByIdAsc(
			String nombre, Long profesorId, com.sistema.panterafitness.enums.Sector sector);

	List<ClaseGimnasio> findByActivaTrueOrderByNombreAsc();

	List<ClaseGimnasio> findByProfesorIdAndActivaTrueOrderByNombreAsc(Long profesorId);
}
