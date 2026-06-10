package com.sistema.panterafitness.service;

import com.sistema.panterafitness.dto.QrSimuladoRequest;
import com.sistema.panterafitness.dto.QrSimuladoResponse;
import com.sistema.panterafitness.entity.Asistencia;
import com.sistema.panterafitness.entity.HorarioClase;
import com.sistema.panterafitness.entity.Notificacion;
import com.sistema.panterafitness.entity.Reserva;
import com.sistema.panterafitness.entity.SectorGimnasio;
import com.sistema.panterafitness.entity.Usuario;
import com.sistema.panterafitness.enums.EstadoAsistencia;
import com.sistema.panterafitness.enums.EstadoReserva;
import com.sistema.panterafitness.enums.MetodoRegistro;
import com.sistema.panterafitness.enums.Rol;
import com.sistema.panterafitness.enums.TipoNotificacion;
import com.sistema.panterafitness.exception.BusinessException;
import com.sistema.panterafitness.exception.ForbiddenException;
import com.sistema.panterafitness.exception.ResourceNotFoundException;
import com.sistema.panterafitness.mapper.EntityMapper;
import com.sistema.panterafitness.repository.AsistenciaRepository;
import com.sistema.panterafitness.repository.ReservaRepository;
import com.sistema.panterafitness.repository.SectorGimnasioRepository;
import com.sistema.panterafitness.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QrSimuladoService {

	private static final List<EstadoReserva> RESERVAS_CONFIRMADAS = List.of(EstadoReserva.CONFIRMADA);

	private final UsuarioRepository usuarioRepository;
	private final ReservaRepository reservaRepository;
	private final AsistenciaRepository asistenciaRepository;
	private final SectorGimnasioRepository sectorGimnasioRepository;
	private final UsuarioService usuarioService;
	private final NotificacionService notificacionService;

	@Transactional
	public QrSimuladoResponse simularIngreso(QrSimuladoRequest request) {
		Usuario autenticado = usuarioService.obtenerUsuarioAutenticado();
		Usuario usuarioQr = usuarioRepository.findByQrSimulado(request.qrSimulado())
				.orElseThrow(() -> new BusinessException("QR simulado no valido."));

		if (autenticado.getRol() == Rol.CLIENTE && !autenticado.getId().equals(usuarioQr.getId())) {
			throw new ForbiddenException("No podes usar el QR de otro usuario.");
		}

		LocalDateTime horaIngreso = request.horaIngresoSimulada() == null
				? LocalDateTime.now()
				: request.horaIngresoSimulada();

		Reserva reserva = resolverReserva(usuarioQr, request.horarioClaseId(), horaIngreso);
		HorarioClase horario = reserva.getHorarioClase();
		EstadoAsistencia estadoAsistencia = esLlegadaTarde(horario, horaIngreso)
				? EstadoAsistencia.AUSENTE
				: EstadoAsistencia.ASISTIDA;

		reserva.setEstadoReserva(estadoAsistencia == EstadoAsistencia.ASISTIDA
				? EstadoReserva.ASISTIDA
				: EstadoReserva.AUSENTE);

		asistenciaRepository.save(Asistencia.builder()
				.usuario(usuarioQr)
				.reserva(reserva)
				.horarioClase(horario)
				.fecha(horario.getFecha())
				.horaProgramada(horario.getHoraInicio())
				.horaIngreso(horaIngreso)
				.estadoAsistencia(estadoAsistencia)
				.metodoRegistro(MetodoRegistro.QR_SIMULADO)
				.build());

		if (estadoAsistencia == EstadoAsistencia.ASISTIDA) {
			incrementarOcupacionSector(horario);
		}

		String mensaje = estadoAsistencia == EstadoAsistencia.ASISTIDA
				? "Ingreso registrado como asistencia simulada."
				: "Llegada tarde: la asistencia fue marcada como AUSENTE.";

		Notificacion notificacion = notificacionService.crear(
				usuarioQr,
				"QR simulado usado",
				mensaje,
				TipoNotificacion.QR
		);

		return new QrSimuladoResponse(
				mensaje,
				reserva.getId(),
				horario.getId(),
				estadoAsistencia,
				horaIngreso,
				EntityMapper.toNotificacionResponse(notificacion)
		);
	}

	private Reserva resolverReserva(Usuario usuario, Long horarioClaseId, LocalDateTime horaIngreso) {
		if (horarioClaseId != null) {
			return reservaRepository.findByUsuarioIdAndHorarioClaseIdAndEstadoReserva(
							usuario.getId(),
							horarioClaseId,
							EstadoReserva.CONFIRMADA
					)
					.orElseThrow(() -> new BusinessException(
							"No tenes una reserva CONFIRMADA para ese horario."
					));
		}

		return reservaRepository.findByUsuarioIdAndEstadoReservaIn(usuario.getId(), RESERVAS_CONFIRMADAS)
				.stream()
				.filter(reserva -> esClaseCercana(reserva.getHorarioClase(), horaIngreso))
				.min(Comparator.comparing(reserva -> reserva.getHorarioClase().getFecha()
						.atTime(reserva.getHorarioClase().getHoraInicio())))
				.orElseThrow(() -> new BusinessException(
						"No tenes una reserva CONFIRMADA para una clase cercana."
				));
	}

	private boolean esClaseCercana(HorarioClase horario, LocalDateTime horaIngreso) {
		LocalDateTime inicio = horario.getFecha().atTime(horario.getHoraInicio());
		LocalDateTime fin = horario.getFecha().atTime(horario.getHoraFin());
		return !horaIngreso.isBefore(inicio.minusMinutes(30)) && !horaIngreso.isAfter(fin.plusMinutes(30));
	}

	private boolean esLlegadaTarde(HorarioClase horario, LocalDateTime horaIngreso) {
		LocalDateTime limite = horario.getFecha().atTime(horario.getHoraInicio()).plusMinutes(10);
		return horaIngreso.isAfter(limite);
	}

	private void incrementarOcupacionSector(HorarioClase horario) {
		SectorGimnasio sector = sectorGimnasioRepository.findByNombre(horario.getClaseGimnasio().getSector())
				.orElseThrow(() -> new ResourceNotFoundException("Sector de gimnasio no encontrado."));
		if (sector.getOcupacionActual() < sector.getCapacidadMaxima()) {
			sector.setOcupacionActual(sector.getOcupacionActual() + 1);
		}
	}
}
