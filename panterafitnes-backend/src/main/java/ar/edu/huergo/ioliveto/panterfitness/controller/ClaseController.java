package ar.edu.huergo.ioliveto.panterfitness.controller;

import ar.edu.huergo.ioliveto.panterfitness.dto.ClaseRequest;
import ar.edu.huergo.ioliveto.panterfitness.dto.ClaseResponse;
import ar.edu.huergo.ioliveto.panterfitness.dto.HorarioClaseResponse;
import ar.edu.huergo.ioliveto.panterfitness.service.ClaseService;
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
@RequestMapping("/api/clases")
@RequiredArgsConstructor
public class ClaseController {

	private final ClaseService claseService;
	private final HorarioClaseService horarioClaseService;

	@GetMapping
	@PreAuthorize("hasAnyRole('CLIENTE','PROFESOR','ADMINISTRADOR')")
	public List<ClaseResponse> listar() {
		return claseService.listarVisibles();
	}

	@GetMapping("/semana")
	@PreAuthorize("hasAnyRole('CLIENTE','PROFESOR','ADMINISTRADOR')")
	public List<HorarioClaseResponse> semana() {
		return horarioClaseService.listarSemana();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('CLIENTE','PROFESOR','ADMINISTRADOR')")
	public ClaseResponse obtener(@PathVariable Long id) {
		return claseService.obtenerPorId(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ClaseResponse crear(@Valid @RequestBody ClaseRequest request) {
		return claseService.crear(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ClaseResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClaseRequest request) {
		return claseService.actualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public void eliminar(@PathVariable Long id) {
		claseService.eliminar(id);
	}
}
