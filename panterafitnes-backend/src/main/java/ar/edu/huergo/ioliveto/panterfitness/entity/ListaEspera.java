package ar.edu.huergo.ioliveto.panterfitness.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "lista_espera")
public class ListaEspera {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "horario_clase_id", nullable = false)
	private HorarioClase horarioClase;

	@Column(nullable = false)
	private LocalDateTime fechaIngreso;

	@Column(nullable = false)
	private Integer posicion;

	@Column(nullable = false)
	private Boolean activa;

	@PrePersist
	void prePersist() {
		if (fechaIngreso == null) {
			fechaIngreso = LocalDateTime.now();
		}
		if (activa == null) {
			activa = true;
		}
	}
}
