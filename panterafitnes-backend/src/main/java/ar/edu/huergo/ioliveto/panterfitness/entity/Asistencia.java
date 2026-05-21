package ar.edu.huergo.ioliveto.panterfitness.entity;

import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoAsistencia;
import ar.edu.huergo.ioliveto.panterfitness.enums.MetodoRegistro;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
@Table(name = "asistencias")
public class Asistencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reserva_id")
	private Reserva reserva;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "horario_clase_id", nullable = false)
	private HorarioClase horarioClase;

	@Column(nullable = false)
	private LocalDate fecha;

	@Column(nullable = false)
	private LocalTime horaProgramada;

	@Column(nullable = false)
	private LocalDateTime horaIngreso;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EstadoAsistencia estadoAsistencia;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MetodoRegistro metodoRegistro;

	@PrePersist
	void prePersist() {
		if (metodoRegistro == null) {
			metodoRegistro = MetodoRegistro.QR_SIMULADO;
		}
		if (estadoAsistencia == null) {
			estadoAsistencia = EstadoAsistencia.PENDIENTE;
		}
	}
}
