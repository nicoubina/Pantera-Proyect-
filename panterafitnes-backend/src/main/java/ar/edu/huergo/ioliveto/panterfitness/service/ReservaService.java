package ar.edu.huergo.ioliveto.panterfitness.service;

import ar.edu.huergo.ioliveto.panterfitness.dto.CancelarReservaResponse;
import ar.edu.huergo.ioliveto.panterfitness.dto.CrearReservaRequest;
import ar.edu.huergo.ioliveto.panterfitness.dto.ReservaResponse;
import ar.edu.huergo.ioliveto.panterfitness.entity.HorarioClase;
import ar.edu.huergo.ioliveto.panterfitness.entity.ListaEspera;
import ar.edu.huergo.ioliveto.panterfitness.entity.Reserva;
import ar.edu.huergo.ioliveto.panterfitness.entity.Usuario;
import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoMembresia;
import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoReserva;
import ar.edu.huergo.ioliveto.panterfitness.enums.Rol;
import ar.edu.huergo.ioliveto.panterfitness.enums.TipoNotificacion;
import ar.edu.huergo.ioliveto.panterfitness.exception.BusinessException;
import ar.edu.huergo.ioliveto.panterfitness.exception.ForbiddenException;
import ar.edu.huergo.ioliveto.panterfitness.exception.ResourceNotFoundException;
import ar.edu.huergo.ioliveto.panterfitness.mapper.EntityMapper;
import ar.edu.huergo.ioliveto.panterfitness.repository.HorarioClaseRepository;
import ar.edu.huergo.ioliveto.panterfitness.repository.ListaEsperaRepository;
import ar.edu.huergo.ioliveto.panterfitness.repository.ReservaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservaService {

	private static final List<EstadoReserva> ESTADOS_RESERVA_ACTIVA = List.of(
			EstadoReserva.CONFIRMADA,
			EstadoReserva.EN_ESPERA,
			EstadoReserva.ASISTIDA,
			EstadoReserva.AUSENTE
	);

	private static final List<EstadoReserva> ESTADOS_SUPERPONIBLES = List.of(
			EstadoReserva.CONFIRMADA,
			EstadoReserva.EN_ESPERA
	);

	private static final List<EstadoReserva> ESTADOS_OCUPAN_CUPO = List.of(
			EstadoReserva.CONFIRMADA,
			EstadoReserva.ASISTIDA
	);

	private final ReservaRepository reservaRepository;
	private final HorarioClaseRepository horarioClaseRepository;
	private final ListaEsperaRepository listaEsperaRepository;
	private final UsuarioService usuarioService;
	private final NotificacionService notificacionService;

	@Transactional
	public ReservaResponse crearReserva(CrearReservaRequest request) {
		Usuario usuario = usuarioService.obtenerUsuarioAutenticado();
		validarPuedeReservar(usuario);

		HorarioClase horario = horarioClaseRepository.findById(request.horarioClaseId())
				.orElseThrow(() -> new ResourceNotFoundException("Horario de clase no encontrado."));
		if (!Boolean.TRUE.equals(horario.getActiva())) {
			throw new BusinessException("El horario de clase no esta activo.");
		}

		validarVentanaReserva(horario);
		validarReservaDuplicada(usuario, horario);
		validarSinSuperposicion(usuario, horario);

		EstadoReserva estado = hayCupoDisponible(horario)
				? EstadoReserva.CONFIRMADA
				: EstadoReserva.EN_ESPERA;

		Reserva reserva = reservaRepository.save(Reserva.builder()
				.usuario(usuario)
				.horarioClase(horario)
				.estadoReserva(estado)
				.build());

		if (estado == EstadoReserva.EN_ESPERA) {
			int posicion = (int) listaEsperaRepository.countByHorarioClaseIdAndActivaTrue(horario.getId()) + 1;
			listaEsperaRepository.save(ListaEspera.builder()
					.usuario(usuario)
					.horarioClase(horario)
					.posicion(posicion)
					.activa(true)
					.build());
			notificacionService.crear(
					usuario,
					"Ingreso a lista de espera",
					"La clase " + horario.getClaseGimnasio().getNombre()
							+ " esta completa. Quedaste en posicion " + posicion + ".",
					TipoNotificacion.LISTA_ESPERA
			);
		} else {
			notificacionService.crear(
					usuario,
					"Reserva confirmada",
					"Tu reserva para " + horario.getClaseGimnasio().getNombre() + " fue confirmada.",
					TipoNotificacion.RESERVA
			);
		}

		return toReservaResponse(reserva);
	}

	@Transactional(readOnly = true)
	public List<ReservaResponse> listarMisReservas() {
		Usuario usuario = usuarioService.obtenerUsuarioAutenticado();
		return reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
				.stream()
				.map(this::toReservaResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<ReservaResponse> listarReservas() {
		Usuario usuario = usuarioService.obtenerUsuarioAutenticado();
		List<Reserva> reservas;
		if (usuario.getRol() == Rol.ADMINISTRADOR) {
			reservas = reservaRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion"));
		} else if (usuario.getRol() == Rol.PROFESOR) {
			reservas = reservaRepository.findByProfesorId(usuario.getId());
		} else {
			reservas = reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId());
		}
		return reservas.stream().map(this::toReservaResponse).toList();
	}

	@Transactional
	public CancelarReservaResponse cancelar(Long reservaId) {
		Usuario usuario = usuarioService.obtenerUsuarioAutenticado();
		Reserva reserva = reservaRepository.findById(reservaId)
				.orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada."));

		boolean esPropia = reserva.getUsuario().getId().equals(usuario.getId());
		boolean esAdmin = usuario.getRol() == Rol.ADMINISTRADOR;
		if (!esPropia && !esAdmin) {
			throw new ForbiddenException("No podes cancelar una reserva de otro usuario.");
		}
		if (reserva.getEstadoReserva() == EstadoReserva.CANCELADA) {
			throw new BusinessException("La reserva ya esta cancelada.");
		}

		validarVentanaCancelacion(reserva.getHorarioClase());

		EstadoReserva estadoAnterior = reserva.getEstadoReserva();
		reserva.setEstadoReserva(EstadoReserva.CANCELADA);
		reserva.setFechaCancelacion(LocalDateTime.now());

		listaEsperaRepository.findByUsuarioIdAndHorarioClaseIdAndActivaTrue(
				reserva.getUsuario().getId(),
				reserva.getHorarioClase().getId()
		).ifPresent(lista -> lista.setActiva(false));

		notificacionService.crear(
				reserva.getUsuario(),
				"Reserva cancelada",
				"Tu reserva para " + reserva.getHorarioClase().getClaseGimnasio().getNombre() + " fue cancelada.",
				TipoNotificacion.CANCELACION
		);

		Reserva promovida = null;
		if (estadoAnterior == EstadoReserva.CONFIRMADA) {
			promovida = promoverPrimeroEnEspera(reserva.getHorarioClase());
		}
		recalcularPosiciones(reserva.getHorarioClase().getId());

		return new CancelarReservaResponse(
				"Cancelacion realizada.",
				toReservaResponse(reserva),
				promovida == null ? null : toReservaResponse(promovida)
		);
	}

	private void validarPuedeReservar(Usuario usuario) {
		if (usuario.getRol() != Rol.CLIENTE) {
			throw new ForbiddenException("Solo usuarios con rol CLIENTE pueden reservar clases.");
		}
		if (usuario.getEstadoMembresia() != EstadoMembresia.ACTIVA) {
			throw new BusinessException("La membresia debe estar ACTIVA para reservar.");
		}
		if (!Boolean.TRUE.equals(usuario.getActivo())) {
			throw new BusinessException("El usuario no esta activo.");
		}
	}

	private void validarVentanaReserva(HorarioClase horario) {
		LocalDateTime ahora = LocalDateTime.now();
		LocalDateTime inicio = inicio(horario);
		if (inicio.isAfter(ahora.plusWeeks(1))) {
			throw new BusinessException("La reserva puede hacerse como maximo con 1 semana de anticipacion.");
		}
		if (inicio.isBefore(ahora.plusMinutes(30))) {
			throw new BusinessException("La reserva debe hacerse como minimo 30 minutos antes del inicio.");
		}
	}

	private void validarVentanaCancelacion(HorarioClase horario) {
		LocalDateTime ahora = LocalDateTime.now();
		LocalDateTime inicio = inicio(horario);
		if (inicio.isBefore(ahora.plusHours(24))) {
			throw new BusinessException("La cancelacion solo se permite hasta 24 horas antes del inicio de la clase.");
		}
	}

	private void validarReservaDuplicada(Usuario usuario, HorarioClase horario) {
		boolean existe = reservaRepository.existsByUsuarioIdAndHorarioClaseIdAndEstadoReservaIn(
				usuario.getId(),
				horario.getId(),
				ESTADOS_RESERVA_ACTIVA
		);
		if (existe) {
			throw new BusinessException("El usuario ya tiene una reserva o lista de espera para ese horario.");
		}
	}

	private void validarSinSuperposicion(Usuario usuario, HorarioClase nuevoHorario) {
		List<Reserva> reservas = reservaRepository.findByUsuarioIdAndEstadoReservaIn(
				usuario.getId(),
				ESTADOS_SUPERPONIBLES
		);
		for (Reserva reserva : reservas) {
			HorarioClase reservado = reserva.getHorarioClase();
			if (reservado.getId().equals(nuevoHorario.getId())) {
				continue;
			}
			if (seSuperponen(reservado, nuevoHorario)) {
				throw new BusinessException("El usuario no puede reservar dos clases superpuestas.");
			}
		}
	}

	private boolean hayCupoDisponible(HorarioClase horario) {
		long ocupados = reservaRepository.countByHorarioClaseIdAndEstadoReservaIn(
				horario.getId(),
				ESTADOS_OCUPAN_CUPO
		);
		return ocupados < horario.getCupoMaximo();
	}

	private Reserva promoverPrimeroEnEspera(HorarioClase horario) {
		List<ListaEspera> esperaActiva = listaEsperaRepository
				.findByHorarioClaseIdAndActivaTrueOrderByFechaIngresoAsc(horario.getId());

		for (ListaEspera lista : esperaActiva) {
			Optional<Reserva> reservaEnEspera = reservaRepository.findByUsuarioIdAndHorarioClaseIdAndEstadoReserva(
					lista.getUsuario().getId(),
					horario.getId(),
					EstadoReserva.EN_ESPERA
			);
			lista.setActiva(false);
			if (reservaEnEspera.isPresent()) {
				Reserva reserva = reservaEnEspera.get();
				reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
				notificacionService.crear(
						reserva.getUsuario(),
						"Cupo asignado",
						"Se libero un cupo y tu reserva para "
								+ horario.getClaseGimnasio().getNombre()
								+ " fue confirmada.",
						TipoNotificacion.LISTA_ESPERA
				);
				return reserva;
			}
		}
		return null;
	}

	private void recalcularPosiciones(Long horarioId) {
		List<ListaEspera> esperaActiva = listaEsperaRepository
				.findByHorarioClaseIdAndActivaTrueOrderByFechaIngresoAsc(horarioId);
		for (int i = 0; i < esperaActiva.size(); i++) {
			esperaActiva.get(i).setPosicion(i + 1);
		}
	}

	private ReservaResponse toReservaResponse(Reserva reserva) {
		Integer posicion = null;
		if (reserva.getEstadoReserva() == EstadoReserva.EN_ESPERA) {
			posicion = listaEsperaRepository.findByUsuarioIdAndHorarioClaseIdAndActivaTrue(
							reserva.getUsuario().getId(),
							reserva.getHorarioClase().getId()
					)
					.map(ListaEspera::getPosicion)
					.orElse(null);
		}
		return EntityMapper.toReservaResponse(reserva, posicion);
	}

	private LocalDateTime inicio(HorarioClase horario) {
		return horario.getFecha().atTime(horario.getHoraInicio());
	}

	private LocalDateTime fin(HorarioClase horario) {
		return horario.getFecha().atTime(horario.getHoraFin());
	}

	private boolean seSuperponen(HorarioClase primero, HorarioClase segundo) {
		return inicio(primero).isBefore(fin(segundo)) && inicio(segundo).isBefore(fin(primero));
	}
}
