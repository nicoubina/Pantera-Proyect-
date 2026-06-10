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

	@Transactional(readOnly = true)
	public List<HorarioClaseResponse> listarVisibles() {
		Usuario actual = usuarioService.obtenerUsuarioAutenticado();
		List<HorarioClase> horarios = actual.getRol() == Rol.PROFESOR
				? horarioClaseRepository.findActivosByProfesorId(actual.getId())
				: horarioClaseRepository.findByActivaTrueOrderByFechaAscHoraInicioAsc();
		return horarios.stream().map(EntityMapper::toHorarioClaseResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<HorarioClaseResponse> listarSemana() {
		LocalDate desde = LocalDate.now();
		LocalDate hasta = desde.plusDays(7);
		Usuario actual = usuarioService.obtenerUsuarioAutenticado();
		List<HorarioClase> horarios = actual.getRol() == Rol.PROFESOR
				? horarioClaseRepository.findActivosByProfesorId(actual.getId())
				: horarioClaseRepository.findByActivaTrueAndFechaBetweenOrderByFechaAscHoraInicioAsc(desde, hasta);
		return horarios.stream()
				.filter(horario -> !horario.getFecha().isBefore(desde) && !horario.getFecha().isAfter(hasta))
				.map(EntityMapper::toHorarioClaseResponse)
				.toList();
	}

	@Transactional
	public HorarioClaseResponse crear(HorarioClaseRequest request) {
		validarHoras(request);
		ClaseGimnasio clase = claseService.obtenerEntidad(request.claseGimnasioId());
		HorarioClase horario = HorarioClase.builder()
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
		HorarioClase horario = obtenerEntidad(id);
		ClaseGimnasio clase = claseService.obtenerEntidad(request.claseGimnasioId());
		horario.setClaseGimnasio(clase);
		horario.setDiaSemana(resolveDiaSemana(request));
		horario.setFecha(request.fecha());
		horario.setHoraInicio(request.horaInicio());
		horario.setHoraFin(request.horaFin());
		horario.setCupoMaximo(request.cupoMaximo() == null ? clase.getCupoMaximo() : request.cupoMaximo());
		horario.setActiva(request.activa() == null || request.activa());
		return EntityMapper.toHorarioClaseResponse(horario);
	}

	@Transactional
	public void eliminar(Long id) {
		HorarioClase horario = obtenerEntidad(id);
		horario.setActiva(false);
	}

	@Transactional(readOnly = true)
	public HorarioClase obtenerEntidad(Long id) {
		return horarioClaseRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Horario de clase no encontrado."));
	}

	private void validarHoras(HorarioClaseRequest request) {
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
