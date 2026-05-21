package ar.edu.huergo.ioliveto.panterfitness.controller;

import ar.edu.huergo.ioliveto.panterfitness.dto.ActualizarMembresiaRequest;
import ar.edu.huergo.ioliveto.panterfitness.dto.UsuarioResponse;
import ar.edu.huergo.ioliveto.panterfitness.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

	private final UsuarioService usuarioService;

	@GetMapping("/me")
	public UsuarioResponse me() {
		return usuarioService.obtenerPerfil();
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public List<UsuarioResponse> listar() {
		return usuarioService.listarTodos();
	}

	@PatchMapping("/{id}/membresia")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public UsuarioResponse actualizarMembresia(
			@PathVariable Long id,
			@Valid @RequestBody ActualizarMembresiaRequest request
	) {
		return usuarioService.actualizarMembresia(id, request);
	}
}
