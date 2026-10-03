package com.sistema.panterafitness.repository;

import com.sistema.panterafitness.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
  java.util.Optional<Asistencia> findByReservaId(Long reservaId);

  boolean existsByReservaId(Long reservaId);

  java.util.List<Asistencia> findByUsuarioIdAndEstadoAsistenciaOrderByFechaAscIdAsc(
      Long usuarioId, com.sistema.panterafitness.enums.EstadoAsistencia estado);
}
