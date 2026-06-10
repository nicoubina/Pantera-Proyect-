package com.sistema.panterafitness.mapper;

import com.sistema.panterafitness.dto.ClaseResponse;
import com.sistema.panterafitness.dto.HorarioClaseResponse;
import com.sistema.panterafitness.dto.NotificacionResponse;
import com.sistema.panterafitness.dto.ReservaResponse;
import com.sistema.panterafitness.dto.UsuarioResponse;
import com.sistema.panterafitness.dto.UsuarioResumenResponse;
import com.sistema.panterafitness.entity.ClaseGimnasio;
import com.sistema.panterafitness.entity.HorarioClase;
import com.sistema.panterafitness.entity.Notificacion;
import com.sistema.panterafitness.entity.Reserva;
import com.sistema.panterafitness.entity.Usuario;

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
