package com.sistema.panterafitness.service;

import com.sistema.panterafitness.entity.*;
import com.sistema.panterafitness.enums.*;
import com.sistema.panterafitness.repository.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(20)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "panterfitness.demo.enabled", havingValue = "true")
public class ModulesDataInitializer implements CommandLineRunner {
  private final UsuarioRepository usuarios;
  private final ClaseGimnasioRepository clases;
  private final HorarioClaseRepository horarios;
  private final ReservaRepository reservas;
  private final AsistenciaRepository asistencias;
  private final ListaEsperaRepository espera;
  private final PenalizacionRepository penalizaciones;
  private final RutinaRepository rutinas;
  private final EjercicioRepository ejercicios;
  private final AlertaRepository alertas;
  private final NotificacionService notificaciones;
  private final PasswordEncoder encoder;

  @Override
  @Transactional
  public void run(String... args) {
    Usuario cliente = usuarios.findByEmail("cliente@panterfitness.com").orElseThrow();
    Usuario profesor = usuarios.findByEmail("profesor@panterfitness.com").orElseThrow();
    ClaseGimnasio clase =
        clases
            .findFirstByNombreAndProfesorIdAndSectorOrderByIdAsc(
                "Funcional", profesor.getId(), Sector.SALA_CLASES)
            .orElseThrow();
    // Seeds aditivos: no reescribir registros existentes, contraseñas ni estados.
    if (asistencias.count() == 0) {
      for (int i = 0; i < 3; i++) {
        LocalDate fecha = LocalDate.now().minusDays(i + 1);
        HorarioClase h =
            horarios.save(
                HorarioClase.builder()
                    .claseGimnasio(clase)
                    .diaSemana(DiaSemana.from(fecha.getDayOfWeek()))
                    .fecha(fecha)
                    .horaInicio(LocalTime.of(16, 0))
                    .horaFin(LocalTime.of(17, 0))
                    .cupoMaximo(20)
                    .activa(true)
                    .build());
        EstadoReserva estado =
            i == 0
                ? EstadoReserva.ASISTIDA
                : i == 1 ? EstadoReserva.AUSENTE : EstadoReserva.CANCELADA;
        Reserva r =
            reservas.save(
                Reserva.builder()
                    .usuario(cliente)
                    .horarioClase(h)
                    .estadoReserva(estado)
                    .fechaCancelacion(i == 2 ? fecha.minusDays(2).atStartOfDay() : null)
                    .build());
        asistencias.save(
            Asistencia.builder()
                .usuario(cliente)
                .reserva(r)
                .horarioClase(h)
                .fecha(fecha)
                .horaProgramada(h.getHoraInicio())
                .horaIngreso(i == 0 ? fecha.atTime(16, 0) : null)
                .estadoAsistencia(EstadoAsistencia.valueOf(estado.name()))
                .metodoRegistro(MetodoRegistro.AUTOMATICO)
                .build());
      }
      var futuros =
          horarios.findByActivaTrueOrderByFechaAscHoraInicioAsc().stream()
              .filter(
                  h ->
                      h.getFecha()
                          .atTime(h.getHoraInicio())
                          .isAfter(LocalDateTime.now().plusHours(24)))
              .toList();
      futuros.stream()
          .filter(
              h ->
                  reservas.countByHorarioClaseIdAndEstadoReservaIn(
                          h.getId(),
                          java.util.List.of(EstadoReserva.CONFIRMADA, EstadoReserva.ASISTIDA))
                      < h.getCupoMaximo())
          .findFirst()
          .ifPresent(
              h -> {
                Reserva r =
                    reservas.save(
                        Reserva.builder()
                            .usuario(cliente)
                            .horarioClase(h)
                            .estadoReserva(EstadoReserva.CONFIRMADA)
                            .build());
                asistencias.save(
                    Asistencia.builder()
                        .usuario(cliente)
                        .reserva(r)
                        .horarioClase(h)
                        .fecha(h.getFecha())
                        .horaProgramada(h.getHoraInicio())
                        .estadoAsistencia(EstadoAsistencia.PENDIENTE)
                        .metodoRegistro(MetodoRegistro.AUTOMATICO)
                        .build());
              });
      futuros.stream()
          .filter(
              h ->
                  reservas.countByHorarioClaseIdAndEstadoReservaIn(
                          h.getId(),
                          java.util.List.of(EstadoReserva.CONFIRMADA, EstadoReserva.ASISTIDA))
                      >= h.getCupoMaximo())
          .findFirst()
          .ifPresent(
              h -> {
                reservas.save(
                    Reserva.builder()
                        .usuario(cliente)
                        .horarioClase(h)
                        .estadoReserva(EstadoReserva.EN_ESPERA)
                        .build());
                espera.save(
                    ListaEspera.builder()
                        .usuario(cliente)
                        .horarioClase(h)
                        .posicion((int) espera.countByHorarioClaseIdAndActivaTrue(h.getId()) + 1)
                        .activa(true)
                        .build());
              });
    }
    if (penalizaciones.count() == 0) {
      Usuario sancionado =
          usuarios
              .findByEmail("sancionado@panterfitness.com")
              .orElseGet(
                  () ->
                      usuarios.save(
                          Usuario.builder()
                              .nombre("Cliente")
                              .apellido("Sancionado")
                              .email("sancionado@panterfitness.com")
                              .password(encoder.encode("123456"))
                              .rol(Rol.CLIENTE)
                              .estadoMembresia(EstadoMembresia.ACTIVA)
                              .fechaInicioMembresia(LocalDate.now().minusDays(10))
                              .fechaVencimientoMembresia(LocalDate.now().plusMonths(1))
                              .activo(true)
                              .qrSimulado("PF-" + java.util.UUID.randomUUID())
                              .build()));
      penalizaciones.save(
          Penalizacion.builder()
              .usuario(sancionado)
              .motivo("Tres ausencias en el ciclo trimestral")
              .fechaInicio(LocalDate.now())
              .fechaFin(LocalDate.now().plusMonths(1).minusDays(1))
              .estado(EstadoPenalizacion.ACTIVA)
              .build());
      penalizaciones.save(
          Penalizacion.builder()
              .usuario(cliente)
              .motivo("Sanción anterior por inasistencias")
              .fechaInicio(LocalDate.now().minusMonths(3))
              .fechaFin(LocalDate.now().minusMonths(2))
              .estado(EstadoPenalizacion.FINALIZADA)
              .build());
      notificaciones.crear(
          sancionado,
          "Penalización activa",
          "Podés reservar con cupo durante los 30 minutos previos al inicio.",
          TipoNotificacion.PENALIZACION);
    }
    if (rutinas.count() == 0) {
      if (ejercicios.count() == 0) {
        ejercicios.save(
            Ejercicio.builder()
                .nombre("Sentadilla")
                .descripcion("Mantener espalda neutra y controlar el descenso.")
                .build());
        ejercicios.save(
            Ejercicio.builder()
                .nombre("Remo con mancuerna")
                .descripcion("Llevar el codo hacia atrás sin girar el tronco.")
                .build());
      }
      Rutina r =
          Rutina.builder()
              .nombre("Fuerza y técnica")
              .descripcion("Plan de entrenamiento supervisado.")
              .profesor(profesor)
              .cliente(cliente)
              .estado(EstadoRutina.ACTIVA)
              .build();
      int orden = 0;
      for (Ejercicio e : ejercicios.findAll())
        r.getEjercicios()
            .add(
                RutinaEjercicio.builder()
                    .rutina(r)
                    .ejercicio(e)
                    .series(3)
                    .repeticiones(12)
                    .pesoSugerido(BigDecimal.valueOf(10))
                    .descanso(60)
                    .orden(++orden)
                    .build());
      rutinas.save(r);
      notificaciones.crear(
          cliente, "Nueva rutina asignada", r.getNombre(), TipoNotificacion.RUTINA);
    }
    if (alertas.count() == 0) {
      Alerta a =
          alertas.save(
              Alerta.builder()
                  .titulo("Recordatorio de ingreso")
                  .descripcion(
                      "Presentá tu credencial y llegá a tiempo para registrar la asistencia a tu"
                          + " clase.")
                  .activa(true)
                  .prioridad(PrioridadAlerta.MEDIA)
                  .build());
      usuarios.findAll().stream()
          .filter(u -> Boolean.TRUE.equals(u.getActivo()))
          .forEach(
              u ->
                  notificaciones.crear(
                      u, a.getTitulo(), a.getDescripcion(), TipoNotificacion.ALERTA));
    }
  }
}
