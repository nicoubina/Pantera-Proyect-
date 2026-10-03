package com.sistema.panterafitness.service;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.sistema.panterafitness.dto.*;
import com.sistema.panterafitness.entity.*;
import com.sistema.panterafitness.enums.*;
import com.sistema.panterafitness.exception.*;
import com.sistema.panterafitness.repository.*;
import com.sistema.panterafitness.security.JwtService;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("h2")
@AutoConfigureMockMvc
@Transactional
class CompleteModulesIntegrationTest {
  @Autowired UsuarioRepository usuarios;
  @Autowired ClaseGimnasioRepository clases;
  @Autowired HorarioClaseRepository horarios;
  @Autowired ReservaRepository reservas;
  @Autowired AsistenciaRepository asistencias;
  @Autowired ListaEsperaRepository espera;
  @Autowired PenalizacionRepository penalizaciones;
  @Autowired RutinaRepository rutinas;
  @Autowired EjercicioRepository ejercicios;
  @Autowired AlertaRepository alertas;
  @Autowired ReservaService reservaService;
  @Autowired AsistenciaService asistenciaService;
  @Autowired AsistenciaCierreService cierre;
  @Autowired PenalizacionService penalizacionService;
  @Autowired RutinaService rutinaService;
  @Autowired QrSimuladoService qr;
  @Autowired AlertaService alertaService;
  @Autowired ModulesDataInitializer initializer;
  @Autowired JwtService jwt;
  @Autowired MockMvc mvc;

  Usuario cliente;
  Usuario profesor;

  @BeforeEach
  void init() {
    cliente = usuario(Rol.CLIENTE);
    profesor = usuarios.findByEmail("profesor@panterfitness.com").orElseThrow();
    autenticar(cliente);
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  Usuario usuario(Rol rol) {
    return usuarios.saveAndFlush(
        Usuario.builder()
            .nombre("Prueba")
            .apellido("Integral")
            .email(UUID.randomUUID() + "@example.test")
            .password("unused-test-hash")
            .rol(rol)
            .estadoMembresia(EstadoMembresia.ACTIVA)
            .fechaInicioMembresia(LocalDate.now().minusDays(1))
            .fechaVencimientoMembresia(LocalDate.now().plusMonths(6))
            .activo(true)
            .qrSimulado("PF-" + UUID.randomUUID())
            .build());
  }

  void autenticar(Usuario u) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities()));
  }

  HorarioClase horario(LocalDateTime inicio, int cupo) {
    ClaseGimnasio c = clases.findAll().getFirst();
    return horarios.saveAndFlush(
        HorarioClase.builder()
            .claseGimnasio(c)
            .fecha(inicio.toLocalDate())
            .diaSemana(DiaSemana.from(inicio.getDayOfWeek()))
            .horaInicio(inicio.toLocalTime())
            .horaFin(inicio.plusMinutes(45).toLocalTime())
            .cupoMaximo(cupo)
            .activa(true)
            .build());
  }

  HorarioClase futuro(int cupo) {
    return horario(LocalDate.now().plusDays(3).atTime(12, 0), cupo);
  }

  Reserva confirmar(Usuario u, HorarioClase h) {
    return reservas.saveAndFlush(
        Reserva.builder()
            .usuario(u)
            .horarioClase(h)
            .estadoReserva(EstadoReserva.CONFIRMADA)
            .build());
  }

  @Test
  void expiredActiveMembershipIsRejectedByDates() {
    cliente.setFechaVencimientoMembresia(LocalDate.now().minusDays(1));
    var h = futuro(2);
    assertThatThrownBy(() -> reservaService.crearReserva(new CrearReservaRequest(h.getId())))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("vigente");
  }

  @Test
  void pendingAttendanceQrAndCancellationShareOneRecord() {
    var h = futuro(2);
    var r = reservaService.crearReserva(new CrearReservaRequest(h.getId()));
    assertThat(asistencias.findByReservaId(r.id()).orElseThrow().getEstadoAsistencia())
        .isEqualTo(EstadoAsistencia.PENDIENTE);
    qr.simularIngreso(
        new QrSimuladoRequest(
            cliente.getQrSimulado(), h.getId(), h.getFecha().atTime(h.getHoraInicio())));
    assertThat(asistencias.findByReservaId(r.id()).orElseThrow().getEstadoAsistencia())
        .isEqualTo(EstadoAsistencia.ASISTIDA);
    assertThat(
            asistencias.findAll().stream()
                .filter(a -> a.getReserva() != null && a.getReserva().getId().equals(r.id()))
                .count())
        .isEqualTo(1);
    assertThatThrownBy(
            () ->
                qr.simularIngreso(
                    new QrSimuladoRequest(
                        cliente.getQrSimulado(),
                        h.getId(),
                        h.getFecha().atTime(h.getHoraInicio()))))
        .isInstanceOf(BusinessException.class);
    var otro = horario(LocalDate.now().plusDays(4).atTime(12, 0), 2);
    var cancelable = reservaService.crearReserva(new CrearReservaRequest(otro.getId()));
    reservaService.cancelar(cancelable.id());
    assertThat(asistencias.findByReservaId(cancelable.id()).orElseThrow().getEstadoAsistencia())
        .isEqualTo(EstadoAsistencia.CANCELADA);
  }

  @Test
  void automaticClosingCreatesAbsenceOnce() {
    var h = horario(LocalDate.now().minusDays(1).atTime(12, 0), 2);
    var r = confirmar(cliente, h);
    cierre.cerrar(r.getId());
    cierre.cerrar(r.getId());
    assertThat(asistencias.findByReservaId(r.getId()).orElseThrow().getEstadoAsistencia())
        .isEqualTo(EstadoAsistencia.AUSENTE);
    assertThat(r.getEstadoReserva()).isEqualTo(EstadoReserva.AUSENTE);
  }

  void falta(LocalDate fecha) {
    var h = horario(fecha.atTime(12, 0), 2);
    var r = confirmar(cliente, h);
    asistenciaService.registrarReserva(
        r, EstadoAsistencia.AUSENTE, null, MetodoRegistro.AUTOMATICO);
  }

  @Test
  void threeAbsencesSanctionOnceAndNextThreeEscalate() {
    falta(LocalDate.now().minusDays(6));
    falta(LocalDate.now().minusDays(5));
    assertThat(penalizaciones.countByUsuarioId(cliente.getId())).isZero();
    falta(LocalDate.now().minusDays(4));
    var primera = penalizaciones.findByUsuarioIdOrderByFechaInicioDesc(cliente.getId()).getFirst();
    assertThat(primera.getFechaFin())
        .isEqualTo(primera.getFechaInicio().plusMonths(1).minusDays(1));
    penalizacionService.evaluarFaltas(cliente);
    assertThat(penalizaciones.countByUsuarioId(cliente.getId())).isEqualTo(1);
    falta(LocalDate.now().minusDays(3));
    falta(LocalDate.now().minusDays(2));
    falta(LocalDate.now().minusDays(1));
    assertThat(penalizaciones.countByUsuarioId(cliente.getId())).isEqualTo(2);
    assertThat(penalizaciones.findByUsuarioIdOrderByFechaInicioDesc(cliente.getId()))
        .anyMatch(p -> p.getFechaFin().equals(p.getFechaInicio().plusMonths(3).minusDays(1)));
  }

  @Test
  void absenceCounterResetsAfterThreeMonths() {
    falta(LocalDate.now().minusMonths(4));
    falta(LocalDate.now().minusMonths(4).plusDays(1));
    falta(LocalDate.now().minusDays(1));
    assertThat(penalizaciones.countByUsuarioId(cliente.getId())).isZero();
  }

  @Test
  void historicalClosedCyclesDoNotCreateNewSanctionsToday() {
    falta(LocalDate.now().minusMonths(4));
    falta(LocalDate.now().minusMonths(4).plusDays(1));
    falta(LocalDate.now().minusMonths(4).plusDays(2));
    falta(LocalDate.now().minusDays(1));
    assertThat(penalizaciones.countByUsuarioId(cliente.getId())).isZero();
  }

  @Test
  void penalizedClientCanOnlyReserveImmediatelyWithVacancy() {
    penalizaciones.save(
        Penalizacion.builder()
            .usuario(cliente)
            .motivo("Sancion")
            .fechaInicio(LocalDate.now())
            .fechaFin(LocalDate.now().plusMonths(1))
            .estado(EstadoPenalizacion.ACTIVA)
            .build());
    var future = futuro(1);
    assertThatThrownBy(() -> reservaService.crearReserva(new CrearReservaRequest(future.getId())))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("penalizacion");
    LocalDateTime now = LocalDateTime.now();
    // Evitar cruzar medianoche en la fecha/hora de la fixture.
    var immediate = horario(now.plusMinutes(15), 1);
    assertThat(
            reservaService.crearReserva(new CrearReservaRequest(immediate.getId())).estadoReserva())
        .isEqualTo(EstadoReserva.CONFIRMADA);
    var full = horario(now.plusMinutes(20), 1);
    confirmar(usuario(Rol.CLIENTE), full);
    assertThatThrownBy(() -> reservaService.crearReserva(new CrearReservaRequest(full.getId())))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("cupo");
  }

  @Test
  void cancellationPromotesQueueAndRecalculatesPosition() {
    var h = futuro(1);
    var r = reservaService.crearReserva(new CrearReservaRequest(h.getId()));
    var waiting = usuario(Rol.CLIENTE);
    autenticar(waiting);
    var wr = reservaService.crearReserva(new CrearReservaRequest(h.getId()));
    assertThat(wr.estadoReserva()).isEqualTo(EstadoReserva.EN_ESPERA);
    var waiting2 = usuario(Rol.CLIENTE);
    autenticar(waiting2);
    reservaService.crearReserva(new CrearReservaRequest(h.getId()));
    autenticar(cliente);
    var response = reservaService.cancelar(r.id());
    assertThat(response.reservaPromovida().usuario().id()).isEqualTo(waiting.getId());
    assertThat(
            espera
                .findByUsuarioIdAndHorarioClaseIdAndActivaTrue(waiting2.getId(), h.getId())
                .orElseThrow()
                .getPosicion())
        .isEqualTo(1);
    assertThat(asistencias.findByReservaId(wr.id()).orElseThrow().getEstadoAsistencia())
        .isEqualTo(EstadoAsistencia.PENDIENTE);
  }

  @Test
  void teacherCanOnlyAssignRelatedClientsAndEditOwnRoutines() {
    var h = futuro(2);
    confirmar(cliente, h);
    autenticar(h.getClaseGimnasio().getProfesor());
    var e = ejercicios.findAll().getFirst();
    var request =
        new RutinaRequest(
            "Plan",
            "Objetivo",
            cliente.getId(),
            EstadoRutina.ACTIVA,
            List.of(new RutinaEjercicioRequest(e.getId(), 3, 10, BigDecimal.TEN, 60)));
    var result = rutinaService.guardar(null, request);
    assertThat(result.ejercicios()).hasSize(1);
    autenticar(cliente);
    assertThat(rutinaService.obtener(result.id()).nombre()).isEqualTo("Plan");
    var unrelated = usuario(Rol.CLIENTE);
    autenticar(unrelated);
    assertThatThrownBy(() -> rutinaService.obtener(result.id()))
        .isInstanceOf(ForbiddenException.class);
    autenticar(usuario(Rol.PROFESOR));
    assertThatThrownBy(() -> rutinaService.guardar(result.id(), request))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  void queuePromotionSkipsExpiredMembershipWithoutOverbooking() {
    var h = futuro(1);
    var r = reservaService.crearReserva(new CrearReservaRequest(h.getId()));
    var blocked = usuario(Rol.CLIENTE);
    autenticar(blocked);
    var waiting = reservaService.crearReserva(new CrearReservaRequest(h.getId()));
    var eligible = usuario(Rol.CLIENTE);
    autenticar(eligible);
    reservaService.crearReserva(new CrearReservaRequest(h.getId()));
    blocked.setFechaVencimientoMembresia(LocalDate.now().minusDays(1));
    usuarios.saveAndFlush(blocked);
    autenticar(cliente);
    var response = reservaService.cancelar(r.id());
    assertThat(response.reservaPromovida().usuario().id()).isEqualTo(eligible.getId());
    assertThat(reservas.findById(waiting.id()).orElseThrow().getEstadoReserva())
        .isEqualTo(EstadoReserva.CANCELADA);
    assertThat(asistencias.findByReservaId(waiting.id()).orElseThrow().getEstadoAsistencia())
        .isEqualTo(EstadoAsistencia.CANCELADA);
    assertThat(
            reservas.countByHorarioClaseIdAndEstadoReservaIn(
                h.getId(), List.of(EstadoReserva.CONFIRMADA, EstadoReserva.ASISTIDA)))
        .isEqualTo(1);
  }

  @Test
  void alertsPersistAndInactiveOnesAreHiddenFromClients() {
    autenticar(usuarios.findByEmail("admin@panterfitness.com").orElseThrow());
    var a =
        alertaService.guardar(
            null, new AlertaRequest("Aviso", "Informacion", true, PrioridadAlerta.ALTA));
    autenticar(cliente);
    assertThat(alertaService.listar()).anyMatch(x -> x.id().equals(a.id()));
    autenticar(usuarios.findByEmail("admin@panterfitness.com").orElseThrow());
    alertaService.guardar(
        a.id(), new AlertaRequest("Aviso", "Informacion", false, PrioridadAlerta.ALTA));
    autenticar(cliente);
    assertThat(alertaService.listar()).noneMatch(x -> x.id().equals(a.id()));
  }

  @Test
  void endpointsEnforceOwnershipRolesAndInactiveJwt() throws Exception {
    var h = futuro(2);
    var other = usuario(Rol.CLIENTE);
    var r = confirmar(other, h);
    var a =
        asistenciaService.registrarReserva(
            r, EstadoAsistencia.ASISTIDA, LocalDateTime.now(), MetodoRegistro.MANUAL);
    String token = jwt.generateToken(cliente);
    SecurityContextHolder.clearContext();
    mvc.perform(get("/api/asistencias/" + a.getId()).header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/estadisticas").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/api/alertas")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(
                    "{\"titulo\":\"X\",\"descripcion\":\"X\",\"activa\":true,\"prioridad\":\"ALTA\"}"))
        .andExpect(status().isForbidden());
    cliente.setActivo(false);
    usuarios.saveAndFlush(cliente);
    mvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void repeatedModuleSeedingPreservesExistingRecords() {
    long count = rutinas.count(),
        alerts = alertas.count(),
        attendance = asistencias.count(),
        penalties = penalizaciones.count();
    initializer.run();
    initializer.run();
    assertThat(rutinas.count()).isEqualTo(count);
    assertThat(alertas.count()).isEqualTo(alerts);
    assertThat(asistencias.count()).isEqualTo(attendance);
    assertThat(penalizaciones.count()).isEqualTo(penalties);
  }
}
