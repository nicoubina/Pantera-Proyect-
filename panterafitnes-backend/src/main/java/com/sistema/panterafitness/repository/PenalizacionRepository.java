package com.sistema.panterafitness.repository;

import com.sistema.panterafitness.entity.Penalizacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PenalizacionRepository extends JpaRepository<Penalizacion, Long> {
  java.util.List<Penalizacion> findByUsuarioIdOrderByFechaInicioDesc(Long usuarioId);

  long countByUsuarioId(Long usuarioId);

  boolean existsByUsuarioIdAndEstadoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
      Long usuarioId,
      com.sistema.panterafitness.enums.EstadoPenalizacion estado,
      java.time.LocalDate inicio,
      java.time.LocalDate fin);
}
