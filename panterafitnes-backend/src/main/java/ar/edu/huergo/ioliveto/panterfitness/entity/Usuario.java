package ar.edu.huergo.ioliveto.panterfitness.entity;

import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoMembresia;
import ar.edu.huergo.ioliveto.panterfitness.enums.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nombre;

	@Column(nullable = false)
	private String apellido;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Rol rol;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EstadoMembresia estadoMembresia;

	private LocalDate fechaInicioMembresia;

	private LocalDate fechaVencimientoMembresia;

	@Column(nullable = false)
	private Boolean activo;

	@Column(nullable = false, unique = true)
	private String qrSimulado;

	@Column(nullable = false, updatable = false)
	private LocalDateTime fechaCreacion;

	@PrePersist
	void prePersist() {
		if (rol == null) {
			rol = Rol.CLIENTE;
		}
		if (estadoMembresia == null) {
			estadoMembresia = EstadoMembresia.PENDIENTE;
		}
		if (activo == null) {
			activo = true;
		}
		if (fechaCreacion == null) {
			fechaCreacion = LocalDateTime.now();
		}
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return Boolean.TRUE.equals(activo);
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return Boolean.TRUE.equals(activo);
	}
}
