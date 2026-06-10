package com.sistema.panterafitness.repository;

import com.sistema.panterafitness.entity.ClaseGimnasio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaseGimnasioRepository extends JpaRepository<ClaseGimnasio, Long> {

	List<ClaseGimnasio> findByActivaTrueOrderByNombreAsc();

	List<ClaseGimnasio> findByProfesorIdAndActivaTrueOrderByNombreAsc(Long profesorId);
}
