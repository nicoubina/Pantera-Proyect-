package ar.edu.huergo.ioliveto.panterfitness.mapper;

import ar.edu.huergo.ioliveto.panterfitness.dto.ClaseResponse;
import ar.edu.huergo.ioliveto.panterfitness.dto.HorarioClaseResponse;
import ar.edu.huergo.ioliveto.panterfitness.dto.NotificacionResponse;
import ar.edu.huergo.ioliveto.panterfitness.dto.ReservaResponse;
import ar.edu.huergo.ioliveto.panterfitness.dto.UsuarioResponse;
import ar.edu.huergo.ioliveto.panterfitness.dto.UsuarioResumenResponse;
import ar.edu.huergo.ioliveto.panterfitness.entity.ClaseGimnasio;
import ar.edu.huergo.ioliveto.panterfitness.entity.HorarioClase;
import ar.edu.huergo.ioliveto.panterfitness.entity.Notificacion;
import ar.edu.huergo.ioliveto.panterfitness.entity.Reserva;
import ar.edu.huergo.ioliveto.panterfitness.entity.Usuario;

public final class EntityMapper {

	private EntityMapper() {
	}

	public static UsuarioResponse toUsuarioResponse(Usuario usuario) {
		return new UsuarioResponse(
				usuario.getId(),
				usuario.getNombre(),
				usuario.getApellido(),
				usuario.getEmail(),
				usuario.getRol(),
				usuario.getEstadoMembresia(),
				usuario.getFechaInicioMembresia(),
				usuario.getFechaVencimientoMembresia(),
				usuario.getActivo(),
				usuario.getQrSimulado(),
				usuario.getFechaCreacion()
		);
	}

	public static UsuarioResumenResponse toUsuarioResumenResponse(Usuario usuario) {
		return new UsuarioResumenResponse(
				usuario.getId(),
				usuario.getNombre(),
				usuario.getApellido(),
				usuario.getEmail(),
				usuario.getRol()
		);
	}

	public static ClaseResponse toClaseResponse(ClaseGimnasio clase) {
		return new ClaseResponse(
				clase.getId(),
				clase.getNombre(),
				clase.getDescripcion(),
				toUsuarioResumenResponse(clase.getProfesor()),
				clase.getSector(),
				clase.getCupoMaximo(),
				clase.getActiva()
		);
	}

	public static HorarioClaseResponse toHorarioClaseResponse(HorarioClase horario) {
		return new HorarioClaseResponse(
				horario.getId(),
				toClaseResponse(horario.getClaseGimnasio()),
				horario.getDiaSemana(),
				horario.getFecha(),
				horario.getHoraInicio(),
				horario.getHoraFin(),
				horario.getCupoMaximo(),
				horario.getActiva()
		);
	}

	public static ReservaResponse toReservaResponse(Reserva reserva, Integer posicionListaEspera) {
		return new ReservaResponse(
				reserva.getId(),
				toUsuarioResumenResponse(reserva.getUsuario()),
				toHorarioClaseResponse(reserva.getHorarioClase()),
				reserva.getEstadoReserva(),
				reserva.getFechaCreacion(),
				reserva.getFechaCancelacion(),
				posicionListaEspera
		);
	}

	public static NotificacionResponse toNotificacionResponse(Notificacion notificacion) {
		return new NotificacionResponse(
				notificacion.getId(),
				notificacion.getTitulo(),
				notificacion.getMensaje(),
				notificacion.getTipoNotificacion(),
				notificacion.getEstadoNotificacion(),
				notificacion.getFechaCreacion()
		);
	}
}
