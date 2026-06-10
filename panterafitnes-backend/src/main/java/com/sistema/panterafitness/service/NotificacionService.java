package com.sistema.panterafitness.service;

import com.sistema.panterafitness.dto.NotificacionResponse;
import com.sistema.panterafitness.entity.Notificacion;
import com.sistema.panterafitness.entity.Usuario;
import com.sistema.panterafitness.enums.EstadoNotificacion;
import com.sistema.panterafitness.enums.Rol;
import com.sistema.panterafitness.enums.TipoNotificacion;
import com.sistema.panterafitness.exception.ForbiddenException;
import com.sistema.panterafitness.exception.ResourceNotFoundException;
import com.sistema.panterafitness.mapper.EntityMapper;
import com.sistema.panterafitness.repository.NotificacionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificacionService {

	private final NotificacionRepository notificacionRepository;
	private final UsuarioService usuarioService;

	@Transactional
	public Notificacion crear(Usuario usuario, String titulo, String mensaje, TipoNotificacion tipo) {
		Notificacion notificacion = Notificacion.builder()
				.usuario(usuario)
				.titulo(titulo)
				.mensaje(mensaje)
				.tipoNotificacion(tipo)
				.estadoNotificacion(EstadoNotificacion.NO_LEIDA)
				.build();
		return notificacionRepository.save(notificacion);
	}

	@Transactional(readOnly = true)
	public List<NotificacionResponse> listarMisNotificaciones() {
		Usuario usuario = usuarioService.obtenerUsuarioAutenticado();
		return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
				.stream()
				.map(EntityMapper::toNotificacionResponse)
				.toList();
	}

	@Transactional
	public NotificacionResponse marcarComoLeida(Long id) {
		Usuario usuario = usuarioService.obtenerUsuarioAutenticado();
		Notificacion notificacion = notificacionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Notificacion no encontrada."));
		boolean esPropia = notificacion.getUsuario().getId().equals(usuario.getId());
		boolean esAdmin = usuario.getRol() == Rol.ADMINISTRADOR;
		if (!esPropia && !esAdmin) {
			throw new ForbiddenException("No podes modificar una notificacion de otro usuario.");
		}
		notificacion.setEstadoNotificacion(EstadoNotificacion.LEIDA);
		return EntityMapper.toNotificacionResponse(notificacion);
	}
}
