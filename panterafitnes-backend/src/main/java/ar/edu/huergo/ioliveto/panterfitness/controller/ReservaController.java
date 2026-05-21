package ar.edu.huergo.ioliveto.panterfitness.controller;

import ar.edu.huergo.ioliveto.panterfitness.dto.CancelarReservaResponse;
import ar.edu.huergo.ioliveto.panterfitness.dto.CrearReservaRequest;
import ar.edu.huergo.ioliveto.panterfitness.dto.ReservaResponse;
import ar.edu.huergo.ioliveto.panterfitness.service.ReservaService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

	private final ReservaService reservaService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('CLIENTE')")
	public ReservaResponse crear(@Valid @RequestBody CrearReservaRequest request) {
		return reservaService.crearReserva(request);
	}

	@GetMapping("/mis-reservas")
	@PreAuthorize("hasRole('CLIENTE')")
	public List<ReservaResponse> misReservas() {
		return reservaService.listarMisReservas();
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('PROFESOR','ADMINISTRADOR')")
	public List<ReservaResponse> listar() {
		return reservaService.listarReservas();
	}

	@DeleteMapping("/{id}/cancelar")
	@PreAuthorize("hasAnyRole('CLIENTE','ADMINISTRADOR')")
	public CancelarReservaResponse cancelar(@PathVariable Long id) {
		return reservaService.cancelar(id);
	}
}
