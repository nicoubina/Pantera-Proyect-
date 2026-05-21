package ar.edu.huergo.ioliveto.panterfitness.service;

import ar.edu.huergo.ioliveto.panterfitness.dto.AuthResponse;
import ar.edu.huergo.ioliveto.panterfitness.dto.LoginRequest;
import ar.edu.huergo.ioliveto.panterfitness.dto.RegistroRequest;
import ar.edu.huergo.ioliveto.panterfitness.entity.Usuario;
import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoMembresia;
import ar.edu.huergo.ioliveto.panterfitness.enums.Rol;
import ar.edu.huergo.ioliveto.panterfitness.exception.BusinessException;
import ar.edu.huergo.ioliveto.panterfitness.mapper.EntityMapper;
import ar.edu.huergo.ioliveto.panterfitness.repository.UsuarioRepository;
import ar.edu.huergo.ioliveto.panterfitness.security.JwtService;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	@Transactional
	public AuthResponse registrar(RegistroRequest request) {
		String email = normalizeEmail(request.email());
		if (usuarioRepository.existsByEmail(email)) {
			throw new BusinessException("Ya existe un usuario registrado con ese email.");
		}

		LocalDate hoy = LocalDate.now();
		Usuario usuario = Usuario.builder()
				.nombre(request.nombre().trim())
				.apellido(normalizeApellido(request.apellido()))
				.email(email)
				.password(passwordEncoder.encode(request.password()))
				.rol(Rol.CLIENTE)
				.estadoMembresia(EstadoMembresia.ACTIVA)
				.fechaInicioMembresia(hoy)
				.fechaVencimientoMembresia(hoy.plusMonths(1))
				.activo(true)
				.qrSimulado(generarQr())
				.build();

		Usuario guardado = usuarioRepository.save(usuario);
		String token = jwtService.generateToken(guardado);
		return new AuthResponse(token, "Bearer", EntityMapper.toUsuarioResponse(guardado));
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		String email = normalizeEmail(request.email());
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new BusinessException("Email o password incorrectos."));
		String token = jwtService.generateToken(usuario);
		return new AuthResponse(token, "Bearer", EntityMapper.toUsuarioResponse(usuario));
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizeApellido(String apellido) {
		if (apellido == null || apellido.isBlank()) {
			return "";
		}
		return apellido.trim();
	}

	private String generarQr() {
		return "PF-" + UUID.randomUUID();
	}
}
