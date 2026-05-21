package ar.edu.huergo.ioliveto.panterfitness.controller;

import ar.edu.huergo.ioliveto.panterfitness.dto.HorarioClaseRequest;
import ar.edu.huergo.ioliveto.panterfitness.dto.HorarioClaseResponse;
import ar.edu.huergo.ioliveto.panterfitness.service.HorarioClaseService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/horarios")
@RequiredArgsConstructor
public class HorarioClaseController {

	private final HorarioClaseService horarioClaseService;

	@GetMapping
	@PreAuthorize("hasAnyRole('CLIENTE','PROFESOR','ADMINISTRADOR')")
	public List<HorarioClaseResponse> listar() {
		return horarioClaseService.listarVisibles();
	}

	@GetMapping("/semana")
	@PreAuthorize("hasAnyRole('CLIENTE','PROFESOR','ADMINISTRADOR')")
	public List<HorarioClaseResponse> semana() {
		return horarioClaseService.listarSemana();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public HorarioClaseResponse crear(@Valid @RequestBody HorarioClaseRequest request) {
		return horarioClaseService.crear(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public HorarioClaseResponse actualizar(
			@PathVariable Long id,
			@Valid @RequestBody HorarioClaseRequest request
	) {
		return horarioClaseService.actualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public void eliminar(@PathVariable Long id) {
		horarioClaseService.eliminar(id);
	}
}
