package ar.edu.huergo.ioliveto.panterfitness.service;

import ar.edu.huergo.ioliveto.panterfitness.dto.ActualizarMembresiaRequest;
import ar.edu.huergo.ioliveto.panterfitness.dto.UsuarioResponse;
import ar.edu.huergo.ioliveto.panterfitness.entity.Usuario;
import ar.edu.huergo.ioliveto.panterfitness.exception.ResourceNotFoundException;
import ar.edu.huergo.ioliveto.panterfitness.exception.UnauthorizedException;
import ar.edu.huergo.ioliveto.panterfitness.mapper.EntityMapper;
import ar.edu.huergo.ioliveto.panterfitness.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;

	@Transactional(readOnly = true)
	public Usuario obtenerUsuarioAutenticado() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			throw new UnauthorizedException("No hay usuario autenticado.");
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof Usuario usuario) {
			return usuarioRepository.findById(usuario.getId())
					.orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado."));
		}

		return usuarioRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado."));
	}

	@Transactional(readOnly = true)
	public UsuarioResponse obtenerPerfil() {
		return EntityMapper.toUsuarioResponse(obtenerUsuarioAutenticado());
	}

	@Transactional(readOnly = true)
	public List<UsuarioResponse> listarTodos() {
		return usuarioRepository.findAll()
				.stream()
				.map(EntityMapper::toUsuarioResponse)
				.toList();
	}

	@Transactional
	public UsuarioResponse actualizarMembresia(Long id, ActualizarMembresiaRequest request) {
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
		usuario.setEstadoMembresia(request.estadoMembresia());
		usuario.setFechaInicioMembresia(request.fechaInicioMembresia());
		usuario.setFechaVencimientoMembresia(request.fechaVencimientoMembresia());
		if (request.activo() != null) {
			usuario.setActivo(request.activo());
		}
		return EntityMapper.toUsuarioResponse(usuario);
	}
}
