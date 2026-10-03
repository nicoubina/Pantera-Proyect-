package com.sistema.panterafitness.repository;

import com.sistema.panterafitness.entity.Reserva;
import com.sistema.panterafitness.enums.EstadoReserva;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select e from Reserva e where e.id = :id")
  java.util.Optional<Reserva> findLockedById(
      @org.springframework.data.repository.query.Param("id") Long id);

  @Query(
      "select r.id from Reserva r where r.estadoReserva ="
          + " com.sistema.panterafitness.enums.EstadoReserva.CONFIRMADA and (r.horarioClase.fecha <"
          + " :fecha or (r.horarioClase.fecha = :fecha and r.horarioClase.horaFin < :hora))")
  java.util.List<Long> findConfirmadasFinalizadas(
      @Param("fecha") java.time.LocalDate fecha, @Param("hora") java.time.LocalTime hora);

  boolean existsByHorarioClaseId(Long horarioId);

  List<Reserva> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

  List<Reserva> findByHorarioClaseIdOrderByFechaCreacionDesc(Long horarioClaseId);

  List<Reserva> findByUsuarioIdAndEstadoReservaIn(
      Long usuarioId, Collection<EstadoReserva> estados);

  Optional<Reserva> findByUsuarioIdAndHorarioClaseIdAndEstadoReserva(
      Long usuarioId, Long horarioClaseId, EstadoReserva estadoReserva);

  boolean existsByUsuarioIdAndHorarioClaseIdAndEstadoReservaIn(
      Long usuarioId, Long horarioClaseId, Collection<EstadoReserva> estados);

  long countByHorarioClaseIdAndEstadoReservaIn(
      Long horarioClaseId, Collection<EstadoReserva> estados);

  @Query(
      """
      select r
      from Reserva r
      where r.horarioClase.claseGimnasio.profesor.id = :profesorId
      order by r.fechaCreacion desc
      """)
  List<Reserva> findByProfesorId(@Param("profesorId") Long profesorId);
}
