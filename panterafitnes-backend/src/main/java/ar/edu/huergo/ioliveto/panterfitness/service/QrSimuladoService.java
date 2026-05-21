package ar.edu.huergo.ioliveto.panterfitness.service;

import ar.edu.huergo.ioliveto.panterfitness.dto.QrSimuladoRequest;
import ar.edu.huergo.ioliveto.panterfitness.dto.QrSimuladoResponse;
import ar.edu.huergo.ioliveto.panterfitness.entity.Asistencia;
import ar.edu.huergo.ioliveto.panterfitness.entity.HorarioClase;
import ar.edu.huergo.ioliveto.panterfitness.entity.Notificacion;
import ar.edu.huergo.ioliveto.panterfitness.entity.Reserva;
import ar.edu.huergo.ioliveto.panterfitness.entity.SectorGimnasio;
import ar.edu.huergo.ioliveto.panterfitness.entity.Usuario;
import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoAsistencia;
import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoReserva;
import ar.edu.huergo.ioliveto.panterfitness.enums.MetodoRegistro;
import ar.edu.huergo.ioliveto.panterfitness.enums.Rol;
import ar.edu.huergo.ioliveto.panterfitness.enums.TipoNotificacion;
import ar.edu.huergo.ioliveto.panterfitness.exception.BusinessException;
import ar.edu.huergo.ioliveto.panterfitness.exception.ForbiddenException;
import ar.edu.huergo.ioliveto.panterfitness.exception.ResourceNotFoundException;
import ar.edu.huergo.ioliveto.panterfitness.mapper.EntityMapper;
import ar.edu.huergo.ioliveto.panterfitness.repository.AsistenciaRepository;
import ar.edu.huergo.ioliveto.panterfitness.repository.ReservaRepository;
import ar.edu.huergo.ioliveto.panterfitness.repository.SectorGimnasioRepository;
import ar.edu.huergo.ioliveto.panterfitness.repository.UsuarioRepository;
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
