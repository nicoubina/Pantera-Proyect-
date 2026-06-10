package com.sistema.panterafitness.controller;

import com.sistema.panterafitness.dto.QrSimuladoRequest;
import com.sistema.panterafitness.dto.QrSimuladoResponse;
import com.sistema.panterafitness.service.QrSimuladoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QrSimuladoController {

	private final QrSimuladoService qrSimuladoService;

	@PostMapping("/simular-ingreso")
	@PreAuthorize("hasAnyRole('CLIENTE','ADMINISTRADOR')")
	public QrSimuladoResponse simularIngreso(@Valid @RequestBody QrSimuladoRequest request) {
		return qrSimuladoService.simularIngreso(request);
	}
}
