package com.sistema.panterafitness.repository;

import com.sistema.panterafitness.entity.SectorGimnasio;
import com.sistema.panterafitness.enums.Sector;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorGimnasioRepository extends JpaRepository<SectorGimnasio, Long> {

	Optional<SectorGimnasio> findByNombre(Sector nombre);

	List<SectorGimnasio> findByActivoTrueOrderByNombreAsc();
}
