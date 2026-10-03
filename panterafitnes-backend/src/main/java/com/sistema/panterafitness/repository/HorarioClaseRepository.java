package com.sistema.panterafitness.repository;

import com.sistema.panterafitness.entity.HorarioClase;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HorarioClaseRepository extends JpaRepository<HorarioClase, Long> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select e from HorarioClase e where e.id = :id")
  java.util.Optional<HorarioClase> findLockedById(
      @org.springframework.data.repository.query.Param("id") Long id);

  java.util.Optional<HorarioClase> findFirstByClaseGimnasioIdAndDiaSemanaAndHoraInicioOrderByIdAsc(
      Long claseId, com.sistema.panterafitness.enums.DiaSemana dia, java.time.LocalTime inicio);

  List<HorarioClase> findByActivaTrueOrderByFechaAscHoraInicioAsc();

  List<HorarioClase> findByActivaTrueAndFechaBetweenOrderByFechaAscHoraInicioAsc(
      LocalDate desde, LocalDate hasta);

  @Query(
      """
      select h
      from HorarioClase h
      where h.activa = true
      and h.claseGimnasio.profesor.id = :profesorId
      order by h.fecha asc, h.horaInicio asc
      """)
  List<HorarioClase> findActivosByProfesorId(@Param("profesorId") Long profesorId);
}
