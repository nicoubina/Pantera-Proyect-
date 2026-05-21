package ar.edu.huergo.ioliveto.panterfitness.service;

import ar.edu.huergo.ioliveto.panterfitness.dto.NotificacionResponse;
import ar.edu.huergo.ioliveto.panterfitness.entity.Notificacion;
import ar.edu.huergo.ioliveto.panterfitness.entity.Usuario;
import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoNotificacion;
import ar.edu.huergo.ioliveto.panterfitness.enums.Rol;
import ar.edu.huergo.ioliveto.panterfitness.enums.TipoNotificacion;
import ar.edu.huergo.ioliveto.panterfitness.exception.ForbiddenException;
import ar.edu.huergo.ioliveto.panterfitness.exception.ResourceNotFoundException;
import ar.edu.huergo.ioliveto.panterfitness.mapper.EntityMapper;
import ar.edu.huergo.ioliveto.panterfitness.repository.NotificacionRepository;
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
