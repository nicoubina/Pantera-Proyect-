package com.sistema.panterafitness.service;

import com.sistema.panterafitness.dto.*;
import com.sistema.panterafitness.entity.*;
import com.sistema.panterafitness.enums.*;
import com.sistema.panterafitness.mapper.EntityMapper;
import com.sistema.panterafitness.repository.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EstadisticasService {
  private final UsuarioRepository usuarios;
  private final ClaseGimnasioRepository clases;
  private final ReservaRepository reservas;
  private final AsistenciaRepository asistencias;
  private final ListaEsperaRepository espera;
  private final MembresiaService membresias;
  private final OcupacionService ocupacion;

  @Transactional(readOnly = true)
  public EstadisticasResponse consultar() {
    var us = usuarios.findAll();
    var cs = clases.findAll();
    var rs = reservas.findAll();
    var as = asistencias.findAll();
    var validas =
        rs.stream()
            .filter(
                r ->
                    r.getEstadoReserva() != EstadoReserva.CANCELADA
                        && r.getEstadoReserva() != EstadoReserva.EN_ESPERA)
            .toList();
    var porClase =
        validas.stream()
            .collect(
                Collectors.groupingBy(
                    r -> r.getHorarioClase().getClaseGimnasio().getId(), Collectors.counting()));
    var porHorario =
        validas.stream()
            .collect(
                Collectors.groupingBy(r -> r.getHorarioClase().getId(), Collectors.counting()));
    var horarios =
        validas.stream()
            .map(Reserva::getHorarioClase)
            .collect(Collectors.toMap(HorarioClase::getId, h -> h, (a, b) -> a));
    var populares =
        cs.stream()
            .map(
                c ->
                    new PopularidadResponse(
                        c.getId(), c.getNombre(), porClase.getOrDefault(c.getId(), 0L)))
            .sorted(Comparator.comparingLong(PopularidadResponse::reservas).reversed())
            .limit(5)
            .toList();
    var utilizados =
        horarios.values().stream()
            .map(
                h ->
                    new UsoHorarioResponse(
                        h.getId(),
                        h.getClaseGimnasio().getNombre(),
                        h.getFecha(),
                        h.getHoraInicio(),
                        porHorario.get(h.getId())))
            .sorted(Comparator.comparingLong(UsoHorarioResponse::reservas).reversed())
            .limit(5)
            .toList();
    var profesores =
        us.stream()
            .filter(u -> u.getRol() == Rol.PROFESOR)
            .map(
                u ->
                    new ProfesorClasesResponse(
                        EntityMapper.toUsuarioResumenResponse(u),
                        cs.stream()
                            .filter(
                                c ->
                                    c.getProfesor().getId().equals(u.getId())
                                        && Boolean.TRUE.equals(c.getActiva()))
                            .count()))
            .toList();
    return new EstadisticasResponse(
        us.size(),
        us.stream()
            .filter(
                u ->
                    u.getRol() == Rol.CLIENTE
                        && membresias.estadoActual(u) == EstadoMembresia.ACTIVA)
            .count(),
        us.stream()
            .filter(
                u ->
                    u.getRol() == Rol.CLIENTE
                        && membresias.estadoActual(u) == EstadoMembresia.VENCIDA)
            .count(),
        cs.size(),
        rs.size(),
        as.stream().filter(a -> a.getEstadoAsistencia() == EstadoAsistencia.ASISTIDA).count(),
        as.stream().filter(a -> a.getEstadoAsistencia() == EstadoAsistencia.AUSENTE).count(),
        espera.countByActivaTrue(),
        ocupacion.obtenerGeneral(),
        populares,
        utilizados,
        profesores);
  }
}
