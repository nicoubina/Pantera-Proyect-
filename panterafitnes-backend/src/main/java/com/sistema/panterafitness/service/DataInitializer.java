package com.sistema.panterafitness.service;

import com.sistema.panterafitness.entity.ClaseGimnasio;
import com.sistema.panterafitness.entity.HorarioClase;
import com.sistema.panterafitness.entity.ListaEspera;
import com.sistema.panterafitness.entity.Notificacion;
import com.sistema.panterafitness.entity.Reserva;
import com.sistema.panterafitness.entity.SectorGimnasio;
import com.sistema.panterafitness.entity.Usuario;
import com.sistema.panterafitness.enums.DiaSemana;
import com.sistema.panterafitness.enums.EstadoMembresia;
import com.sistema.panterafitness.enums.EstadoNotificacion;
import com.sistema.panterafitness.enums.EstadoReserva;
import com.sistema.panterafitness.enums.Rol;
import com.sistema.panterafitness.enums.Sector;
import com.sistema.panterafitness.enums.TipoNotificacion;
import com.sistema.panterafitness.repository.ClaseGimnasioRepository;
import com.sistema.panterafitness.repository.HorarioClaseRepository;
import com.sistema.panterafitness.repository.ListaEsperaRepository;
import com.sistema.panterafitness.repository.NotificacionRepository;
import com.sistema.panterafitness.repository.ReservaRepository;
import com.sistema.panterafitness.repository.SectorGimnasioRepository;
import com.sistema.panterafitness.repository.UsuarioRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

	private final UsuarioRepository usuarioRepository;
	private final SectorGimnasioRepository sectorGimnasioRepository;
	private final ClaseGimnasioRepository claseGimnasioRepository;
	private final HorarioClaseRepository horarioClaseRepository;
	private final ReservaRepository reservaRepository;
	private final ListaEsperaRepository listaEsperaRepository;
	private final NotificacionRepository notificacionRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void run(String... args) {
		if (usuarioRepository.count() > 0) {
			return;
		}

		LocalDate hoy = LocalDate.now();
		Usuario clienteActivo = crearUsuario(
				"Cliente",
				"Activo",
				"cliente@panterfitness.com",
				Rol.CLIENTE,
				EstadoMembresia.ACTIVA,
				hoy.minusMonths(1),
				hoy.plusMonths(1),
				"QR-CLIENTE-ACTIVO"
		);
		crearUsuario(
				"Cliente",
				"Vencido",
				"vencido@panterfitness.com",
				Rol.CLIENTE,
				EstadoMembresia.VENCIDA,
				hoy.minusMonths(2),
				hoy.minusDays(1),
				"QR-CLIENTE-VENCIDO"
		);
		Usuario profesor = crearUsuario(
				"Profesor",
				"Demo",
				"profesor@panterfitness.com",
				Rol.PROFESOR,
				EstadoMembresia.ACTIVA,
				hoy.minusMonths(1),
				hoy.plusYears(1),
				"QR-PROFESOR-DEMO"
		);
		crearUsuario(
				"Admin",
				"Demo",
				"admin@panterfitness.com",
				Rol.ADMINISTRADOR,
				EstadoMembresia.ACTIVA,
				hoy.minusMonths(1),
				hoy.plusYears(1),
				"QR-ADMIN-DEMO"
		);

		sectorGimnasioRepository.save(SectorGimnasio.builder()
				.nombre(Sector.MUSCULACION)
				.capacidadMaxima(40)
				.ocupacionActual(18)
				.activo(true)
				.build());
		sectorGimnasioRepository.save(SectorGimnasio.builder()
				.nombre(Sector.SALA_CLASES)
				.capacidadMaxima(20)
				.ocupacionActual(7)
				.activo(true)
				.build());

		ClaseGimnasio funcional = claseGimnasioRepository.save(ClaseGimnasio.builder()
				.nombre("Funcional")
				.descripcion("Entrenamiento funcional grupal para fuerza y resistencia.")
				.profesor(profesor)
				.sector(Sector.SALA_CLASES)
				.cupoMaximo(20)
				.activa(true)
				.build());
		ClaseGimnasio musculacion = claseGimnasioRepository.save(ClaseGimnasio.builder()
				.nombre("Musculacion")
				.descripcion("Clase guiada de tecnica y rutina basica de musculacion.")
				.profesor(profesor)
				.sector(Sector.MUSCULACION)
				.cupoMaximo(20)
				.activa(true)
				.build());

		HorarioClase funcionalLunes = crearHorario(funcional, DayOfWeek.MONDAY, LocalTime.of(18, 0), LocalTime.of(19, 0));
		crearHorario(funcional, DayOfWeek.WEDNESDAY, LocalTime.of(18, 0), LocalTime.of(19, 0));
		HorarioClase funcionalViernes = crearHorario(funcional, DayOfWeek.FRIDAY, LocalTime.of(18, 0), LocalTime.of(19, 0));
		crearHorario(musculacion, DayOfWeek.TUESDAY, LocalTime.of(19, 0), LocalTime.of(20, 0));
		crearHorario(musculacion, DayOfWeek.THURSDAY, LocalTime.of(19, 0), LocalTime.of(20, 0));

		sembrarHorarioLleno(funcionalViernes);
		crearNotificacionBienvenida(clienteActivo);
		crearNotificacionSistema(profesor, "Tenes clases asignadas para la semana.");
	}

	private Usuario crearUsuario(
			String nombre,
			String apellido,
			String email,
			Rol rol,
			EstadoMembresia estadoMembresia,
			LocalDate inicio,
			LocalDate vencimiento,
			String qr
	) {
		return usuarioRepository.save(Usuario.builder()
				.nombre(nombre)
				.apellido(apellido)
				.email(email)
				.password(passwordEncoder.encode("123456"))
				.rol(rol)
				.estadoMembresia(estadoMembresia)
				.fechaInicioMembresia(inicio)
				.fechaVencimientoMembresia(vencimiento)
				.activo(true)
				.qrSimulado(qr)
				.build());
	}

	private HorarioClase crearHorario(ClaseGimnasio clase, DayOfWeek dayOfWeek, LocalTime inicio, LocalTime fin) {
		LocalDate fecha = proximaFecha(dayOfWeek, inicio);
		return horarioClaseRepository.save(HorarioClase.builder()
				.claseGimnasio(clase)
				.diaSemana(DiaSemana.from(dayOfWeek))
				.fecha(fecha)
				.horaInicio(inicio)
				.horaFin(fin)
				.cupoMaximo(clase.getCupoMaximo())
				.activa(true)
				.build());
	}

	private LocalDate proximaFecha(DayOfWeek dayOfWeek, LocalTime inicio) {
		LocalDate hoy = LocalDate.now();
		int dias = (dayOfWeek.getValue() - hoy.getDayOfWeek().getValue() + 7) % 7;
		LocalDate fecha = hoy.plusDays(dias);
		if (!fecha.atTime(inicio).isAfter(LocalDateTime.now().plusMinutes(30))) {
			fecha = fecha.plusWeeks(1);
		}
		return fecha;
	}

	private void sembrarHorarioLleno(HorarioClase horario) {
		List<Usuario> clientes = new ArrayList<>();
		for (int i = 1; i <= horario.getCupoMaximo(); i++) {
			String numero = String.format("%02d", i);
			clientes.add(crearUsuario(
					"Cliente",
					"Cupo " + numero,
					"cupo" + numero + "@panterfitness.com",
					Rol.CLIENTE,
					EstadoMembresia.ACTIVA,
					LocalDate.now().minusMonths(1),
					LocalDate.now().plusMonths(1),
					"QR-CUPO-" + numero
			));
		}

		for (Usuario cliente : clientes) {
			reservaRepository.save(Reserva.builder()
					.usuario(cliente)
					.horarioClase(horario)
					.estadoReserva(EstadoReserva.CONFIRMADA)
					.build());
		}

		Usuario esperaDemo = crearUsuario(
				"Cliente",
				"Espera",
				"espera@panterfitness.com",
				Rol.CLIENTE,
				EstadoMembresia.ACTIVA,
				LocalDate.now().minusMonths(1),
				LocalDate.now().plusMonths(1),
				"QR-CLIENTE-ESPERA"
		);
		reservaRepository.save(Reserva.builder()
				.usuario(esperaDemo)
				.horarioClase(horario)
				.estadoReserva(EstadoReserva.EN_ESPERA)
				.build());
		listaEsperaRepository.save(ListaEspera.builder()
				.usuario(esperaDemo)
				.horarioClase(horario)
				.posicion(1)
				.activa(true)
				.build());
		crearNotificacionSistema(esperaDemo, "Quedaste en lista de espera para Funcional.");
	}

	private void crearNotificacionBienvenida(Usuario usuario) {
		crearNotificacionSistema(usuario, "Bienvenido a panterfitness. Tu usuario de prueba esta listo.");
	}

	private void crearNotificacionSistema(Usuario usuario, String mensaje) {
		notificacionRepository.save(Notificacion.builder()
				.usuario(usuario)
				.titulo("Sistema")
				.mensaje(mensaje)
				.tipoNotificacion(TipoNotificacion.SISTEMA)
				.estadoNotificacion(EstadoNotificacion.NO_LEIDA)
				.build());
	}
}
