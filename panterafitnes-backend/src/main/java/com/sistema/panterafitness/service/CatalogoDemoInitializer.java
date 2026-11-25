package com.sistema.panterafitness.service;

import com.sistema.panterafitness.entity.ClaseGimnasio;
import com.sistema.panterafitness.entity.HorarioClase;
import com.sistema.panterafitness.entity.Usuario;
import com.sistema.panterafitness.enums.DiaSemana;
import com.sistema.panterafitness.enums.EstadoMembresia;
import com.sistema.panterafitness.enums.Rol;
import com.sistema.panterafitness.enums.Sector;
import com.sistema.panterafitness.repository.ClaseGimnasioRepository;
import com.sistema.panterafitness.repository.HorarioClaseRepository;
import com.sistema.panterafitness.repository.UsuarioRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Catalogo de demostracion ampliado: una segunda profesora y tres clases mas con su agenda
 * semanal recurrente. Permite recorrer la grilla, la ocupacion por sector y las vistas de
 * profesores con datos realistas. Es aditivo e idempotente, igual que {@link DataInitializer}.
 */
@Component
@Order(15)
@ConditionalOnProperty(name = "panterfitness.demo.enabled", havingValue = "true")
@RequiredArgsConstructor
public class CatalogoDemoInitializer implements CommandLineRunner {

  private final UsuarioRepository usuarioRepository;
  private final ClaseGimnasioRepository claseRepository;
  private final HorarioClaseRepository horarioRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(String... args) {
    Usuario profesor = usuarioRepository.findByEmail("profesor@panterfitness.com").orElse(null);
    if (profesor == null) {
      return;
    }
    Usuario profesora = profesora();

    ClaseGimnasio yoga =
        clase("Yoga", "Movilidad, respiracion y flexibilidad para todos los niveles.", profesora,
            Sector.SALA_CLASES, 15);
    ClaseGimnasio spinning =
        clase("Spinning", "Ciclismo indoor de alta intensidad con musica.", profesora,
            Sector.SALA_CLASES, 12);
    ClaseGimnasio crossfit =
        clase("Crossfit", "Entrenamiento funcional de alta intensidad por estaciones.", profesor,
            Sector.MUSCULACION, 14);

    horario(yoga, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0));
    horario(yoga, DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(10, 0));
    horario(yoga, DayOfWeek.SATURDAY, LocalTime.of(10, 0), LocalTime.of(11, 0));
    horario(spinning, DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(8, 45));
    horario(spinning, DayOfWeek.THURSDAY, LocalTime.of(8, 0), LocalTime.of(8, 45));
    horario(crossfit, DayOfWeek.MONDAY, LocalTime.of(20, 0), LocalTime.of(21, 0));
    horario(crossfit, DayOfWeek.WEDNESDAY, LocalTime.of(20, 0), LocalTime.of(21, 0));
    horario(crossfit, DayOfWeek.FRIDAY, LocalTime.of(20, 0), LocalTime.of(21, 0));
  }

  /** Renueva la agenda del catalogo demo todos los dias a las 00:15 (idempotente). */
  @Scheduled(cron = "0 15 0 * * *")
  @Transactional
  public void renovarAgenda() {
    run();
  }

  private Usuario profesora() {
    return usuarioRepository
        .findByEmail("profesora@panterfitness.com")
        .orElseGet(
            () ->
                usuarioRepository.save(
                    Usuario.builder()
                        .nombre("Lucia")
                        .apellido("Fernandez")
                        .email("profesora@panterfitness.com")
                        .password(passwordEncoder.encode("123456"))
                        .rol(Rol.PROFESOR)
                        .estadoMembresia(EstadoMembresia.ACTIVA)
                        .fechaInicioMembresia(LocalDate.now().minusMonths(1))
                        .fechaVencimientoMembresia(LocalDate.now().plusYears(1))
                        .activo(true)
                        .codigoQr("QR-PROFESORA-DEMO")
                        .build()));
  }

  private ClaseGimnasio clase(
      String nombre, String descripcion, Usuario profesor, Sector sector, int cupo) {
    return claseRepository
        .findFirstByNombreAndProfesorIdAndSectorOrderByIdAsc(nombre, profesor.getId(), sector)
        .orElseGet(
            () ->
                claseRepository.save(
                    ClaseGimnasio.builder()
                        .nombre(nombre)
                        .descripcion(descripcion)
                        .profesor(profesor)
                        .sector(sector)
                        .cupoMaximo(cupo)
                        .activa(true)
                        .build()));
  }

  private void horario(ClaseGimnasio clase, DayOfWeek dia, LocalTime inicio, LocalTime fin) {
    boolean existeProxima =
        horarioRepository
            .findFirstByClaseGimnasioIdAndDiaSemanaAndHoraInicioAndFechaGreaterThanEqualOrderByFechaAsc(
                clase.getId(), DiaSemana.from(dia), inicio, LocalDate.now())
            .isPresent();
    if (existeProxima) {
      return;
    }
    LocalDate hoy = LocalDate.now();
    LocalDate fecha = hoy.plusDays((dia.getValue() - hoy.getDayOfWeek().getValue() + 7) % 7);
    if (!fecha.atTime(inicio).isAfter(LocalDateTime.now().plusMinutes(30))) {
      fecha = fecha.plusWeeks(1);
    }
    horarioRepository.save(
        HorarioClase.builder()
            .claseGimnasio(clase)
            .diaSemana(DiaSemana.from(dia))
            .fecha(fecha)
            .horaInicio(inicio)
            .horaFin(fin)
            .cupoMaximo(clase.getCupoMaximo())
            .activa(true)
            .build());
  }
}
