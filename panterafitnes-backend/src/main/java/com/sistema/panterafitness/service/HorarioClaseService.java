package com.sistema.panterafitness.service;

import com.sistema.panterafitness.dto.HorarioClaseRequest;
import com.sistema.panterafitness.dto.HorarioClaseResponse;
import com.sistema.panterafitness.entity.ClaseGimnasio;
import com.sistema.panterafitness.entity.HorarioClase;
import com.sistema.panterafitness.entity.Usuario;
import com.sistema.panterafitness.enums.DiaSemana;
import com.sistema.panterafitness.enums.Rol;
import com.sistema.panterafitness.exception.BusinessException;
import com.sistema.panterafitness.exception.ResourceNotFoundException;
import com.sistema.panterafitness.mapper.EntityMapper;
import com.sistema.panterafitness.repository.HorarioClaseRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HorarioClaseService {

  private final HorarioClaseRepository horarioClaseRepository;
  private final ClaseService claseService;
  private final UsuarioService usuarioService;
  private final com.sistema.panterafitness.repository.ReservaRepository reservas;

  @Transactional(readOnly = true)
  public List<HorarioClaseResponse> listarVisibles() {
    Usuario actual = usuarioService.obtenerUsuarioAutenticado();
    List<HorarioClase> horarios =
        actual.getRol() == Rol.PROFESOR
            ? horarioClaseRepository.findActivosByProfesorId(actual.getId())
            : actual.getRol() == Rol.ADMINISTRADOR
                ? horarioClaseRepository.findAll()
                : horarioClaseRepository.findByActivaTrueOrderByFechaAscHoraInicioAsc();
    return horarios.stream().map(EntityMapper::toHorarioClaseResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<HorarioClaseResponse> listarSemana() {
    LocalDate desde = LocalDate.now();
    LocalDate hasta = desde.plusDays(7);
    Usuario actual = usuarioService.obtenerUsuarioAutenticado();
    List<HorarioClase> horarios =
        actual.getRol() == Rol.PROFESOR
            ? horarioClaseRepository.findActivosByProfesorId(actual.getId())
            : horarioClaseRepository.findByActivaTrueAndFechaBetweenOrderByFechaAscHoraInicioAsc(
                desde, hasta);
    return horarios.stream()
        .filter(
            horario -> !horario.getFecha().isBefore(desde) && !horario.getFecha().isAfter(hasta))
        .map(EntityMapper::toHorarioClaseResponse)
        .toList();
  }

  @Transactional
  public HorarioClaseResponse crear(HorarioClaseRequest request) {
    validarHoras(request);
    ClaseGimnasio clase = claseService.obtenerEntidad(request.claseGimnasioId());
    HorarioClase horario =
        HorarioClase.builder()
            .claseGimnasio(clase)
            .diaSemana(resolveDiaSemana(request))
            .fecha(request.fecha())
            .horaInicio(request.horaInicio())
            .horaFin(request.horaFin())
            .cupoMaximo(request.cupoMaximo() == null ? clase.getCupoMaximo() : request.cupoMaximo())
            .activa(request.activa() == null || request.activa())
            .build();
    return EntityMapper.toHorarioClaseResponse(horarioClaseRepository.save(horario));
  }

  @Transactional
  public HorarioClaseResponse actualizar(Long id, HorarioClaseRequest request) {
    validarHoras(request);
    HorarioClase horario =
        horarioClaseRepository
            .findLockedById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado."));
    int cupo =
        request.cupoMaximo() == null
            ? claseService.obtenerEntidad(request.claseGimnasioId()).getCupoMaximo()
            : request.cupoMaximo();
    if (cupo
        < reservas.countByHorarioClaseIdAndEstadoReservaIn(
            id,
            java.util.List.of(
                com.sistema.panterafitness.enums.EstadoReserva.CONFIRMADA,
                com.sistema.panterafitness.enums.EstadoReserva.ASISTIDA)))
      throw new BusinessException("El cupo no puede ser menor que las reservas confirmadas.");
    if (reservas.existsByHorarioClaseId(id)
        && (!horario.getClaseGimnasio().getId().equals(request.claseGimnasioId())
            || !horario.getFecha().equals(request.fecha())
            || !horario.getHoraInicio().equals(request.horaInicio())
            || !horario.getHoraFin().equals(request.horaFin())
            || Boolean.FALSE.equals(request.activa())))
      throw new BusinessException(
          "Un horario con reservas conserva clase, fecha, horas y estado. Crea otro horario.");
    ClaseGimnasio clase = claseService.obtenerEntidad(request.claseGimnasioId());
    horario.setClaseGimnasio(clase);
    horario.setDiaSemana(resolveDiaSemana(request));
    horario.setFecha(request.fecha());
    horario.setHoraInicio(request.horaInicio());
    horario.setHoraFin(request.horaFin());
    horario.setCupoMaximo(
        request.cupoMaximo() == null ? clase.getCupoMaximo() : request.cupoMaximo());
    horario.setActiva(request.activa() == null || request.activa());
    return EntityMapper.toHorarioClaseResponse(horario);
  }

  @Transactional
  public void eliminar(Long id) {
    HorarioClase horario = obtenerEntidad(id);
    if (reservas.existsByHorarioClaseId(id))
      throw new BusinessException(
          "No se puede eliminar un horario con reservas. Conserva el historial.");
    horario.setActiva(false);
  }

  @Transactional(readOnly = true)
  public HorarioClase obtenerEntidad(Long id) {
    return horarioClaseRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Horario de clase no encontrado."));
  }

  private void validarHoras(HorarioClaseRequest request) {
    if (request.diaSemana() != null
        && request.diaSemana() != DiaSemana.from(request.fecha().getDayOfWeek()))
      throw new BusinessException("El dia de semana no coincide con la fecha.");
    if (!request.horaFin().isAfter(request.horaInicio())) {
      throw new BusinessException("La hora de fin debe ser posterior a la hora de inicio.");
    }
  }

  private DiaSemana resolveDiaSemana(HorarioClaseRequest request) {
    if (request.diaSemana() != null) {
      return request.diaSemana();
    }
    return DiaSemana.from(request.fecha().getDayOfWeek());
  }
}
