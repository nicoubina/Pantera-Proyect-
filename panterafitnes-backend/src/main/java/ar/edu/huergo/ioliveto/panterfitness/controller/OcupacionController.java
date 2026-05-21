package ar.edu.huergo.ioliveto.panterfitness.controller;

import ar.edu.huergo.ioliveto.panterfitness.dto.OcupacionClaseResponse;
import ar.edu.huergo.ioliveto.panterfitness.dto.OcupacionGeneralResponse;
import ar.edu.huergo.ioliveto.panterfitness.dto.OcupacionSectorResponse;
import ar.edu.huergo.ioliveto.panterfitness.service.OcupacionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ocupacion")
@RequiredArgsConstructor
public class OcupacionController {

	private final OcupacionService ocupacionService;

	@GetMapping("/general")
	@PreAuthorize("hasAnyRole('CLIENTE','PROFESOR','ADMINISTRADOR')")
	public OcupacionGeneralResponse general() {
		return ocupacionService.obtenerGeneral();
	}

	@GetMapping("/sectores")
	@PreAuthorize("hasAnyRole('CLIENTE','PROFESOR','ADMINISTRADOR')")
	public List<OcupacionSectorResponse> sectores() {
		return ocupacionService.obtenerSectores();
	}

	@GetMapping("/clases")
	@PreAuthorize("hasAnyRole('CLIENTE','PROFESOR','ADMINISTRADOR')")
	public List<OcupacionClaseResponse> clases() {
		return ocupacionService.obtenerClases();
	}

	@GetMapping("/clases/{horarioId}")
	@PreAuthorize("hasAnyRole('CLIENTE','PROFESOR','ADMINISTRADOR')")
	public OcupacionClaseResponse clase(@PathVariable Long horarioId) {
		return ocupacionService.obtenerClase(horarioId);
	}
}
