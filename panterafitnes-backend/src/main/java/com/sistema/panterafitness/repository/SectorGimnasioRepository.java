package com.sistema.panterafitness.repository;

import com.sistema.panterafitness.entity.SectorGimnasio;
import com.sistema.panterafitness.enums.Sector;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorGimnasioRepository extends JpaRepository<SectorGimnasio, Long> {

  Optional<SectorGimnasio> findByNombre(Sector nombre);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query(
      "select s from SectorGimnasio s where s.nombre = :nombre")
  Optional<SectorGimnasio> findLockedByNombre(
      @org.springframework.data.repository.query.Param("nombre") Sector nombre);

  List<SectorGimnasio> findByActivoTrueOrderByNombreAsc();
}
