package ar.edu.huergo.ioliveto.panterfitness.entity;

import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoNotificacion;
import ar.edu.huergo.ioliveto.panterfitness.enums.TipoNotificacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notificaciones")
public class Notificacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Column(nullable = false)
	private String titulo;

	@Column(nullable = false, length = 1000)
	private String mensaje;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoNotificacion tipoNotificacion;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EstadoNotificacion estadoNotificacion;

	@Column(nullable = false, updatable = false)
	private LocalDateTime fechaCreacion;

	@PrePersist
	void prePersist() {
		if (estadoNotificacion == null) {
			estadoNotificacion = EstadoNotificacion.NO_LEIDA;
		}
		if (fechaCreacion == null) {
			fechaCreacion = LocalDateTime.now();
		}
	}
}
