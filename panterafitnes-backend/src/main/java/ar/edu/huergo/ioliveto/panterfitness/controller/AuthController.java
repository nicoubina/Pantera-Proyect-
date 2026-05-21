package ar.edu.huergo.ioliveto.panterfitness.controller;

import ar.edu.huergo.ioliveto.panterfitness.dto.AuthResponse;
import ar.edu.huergo.ioliveto.panterfitness.dto.LoginRequest;
import ar.edu.huergo.ioliveto.panterfitness.dto.RegistroRequest;
import ar.edu.huergo.ioliveto.panterfitness.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/registro")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse registrar(@Valid @RequestBody RegistroRequest request) {
		return authService.registrar(request);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}
}
