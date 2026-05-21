package ar.edu.huergo.ioliveto.panterfitness.entity;

import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoReserva;
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
@Table(name = "reservas")
public class Reserva {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "horario_clase_id", nullable = false)
	private HorarioClase horarioClase;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EstadoReserva estadoReserva;

	@Column(nullable = false, updatable = false)
	private LocalDateTime fechaCreacion;

	private LocalDateTime fechaCancelacion;

	@PrePersist
	void prePersist() {
		if (estadoReserva == null) {
			estadoReserva = EstadoReserva.CONFIRMADA;
		}
		if (fechaCreacion == null) {
			fechaCreacion = LocalDateTime.now();
		}
	}
}
