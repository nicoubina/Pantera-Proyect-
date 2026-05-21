package ar.edu.huergo.ioliveto.panterfitness.repository;

import ar.edu.huergo.ioliveto.panterfitness.entity.SectorGimnasio;
import ar.edu.huergo.ioliveto.panterfitness.enums.Sector;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorGimnasioRepository extends JpaRepository<SectorGimnasio, Long> {

	Optional<SectorGimnasio> findByNombre(Sector nombre);

	List<SectorGimnasio> findByActivoTrueOrderByNombreAsc();
}
