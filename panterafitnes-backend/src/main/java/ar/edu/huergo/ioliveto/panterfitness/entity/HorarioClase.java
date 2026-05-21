package ar.edu.huergo.ioliveto.panterfitness.entity;

import ar.edu.huergo.ioliveto.panterfitness.enums.DiaSemana;
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
@Table(name = "horarios_clase")
public class HorarioClase {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "clase_gimnasio_id", nullable = false)
	private ClaseGimnasio claseGimnasio;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DiaSemana diaSemana;

	@Column(nullable = false)
	private LocalDate fecha;

	@Column(nullable = false)
	private LocalTime horaInicio;

	@Column(nullable = false)
	private LocalTime horaFin;

	@Column(nullable = false)
	private Integer cupoMaximo;

	@Column(nullable = false)
	private Boolean activa;

	@PrePersist
	void prePersist() {
		if (cupoMaximo == null && claseGimnasio != null) {
			cupoMaximo = claseGimnasio.getCupoMaximo();
		}
		if (cupoMaximo == null) {
			cupoMaximo = 20;
		}
		if (activa == null) {
			activa = true;
		}
	}
}
