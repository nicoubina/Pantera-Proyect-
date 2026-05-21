package ar.edu.huergo.ioliveto.panterfitness.controller;

import ar.edu.huergo.ioliveto.panterfitness.dto.NotificacionResponse;
import ar.edu.huergo.ioliveto.panterfitness.service.NotificacionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

	private final NotificacionService notificacionService;

	@GetMapping("/mis-notificaciones")
	@PreAuthorize("isAuthenticated()")
	public List<NotificacionResponse> misNotificaciones() {
		return notificacionService.listarMisNotificaciones();
	}

	@PatchMapping("/{id}/leer")
	@PreAuthorize("isAuthenticated()")
	public NotificacionResponse leer(@PathVariable Long id) {
		return notificacionService.marcarComoLeida(id);
	}
}
