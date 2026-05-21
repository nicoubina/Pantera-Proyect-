package ar.edu.huergo.ioliveto.panterfitness.entity;

import ar.edu.huergo.ioliveto.panterfitness.enums.Sector;
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
@Table(name = "clases_gimnasio")
public class ClaseGimnasio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nombre;

	@Column(length = 1000)
	private String descripcion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "profesor_id", nullable = false)
	private Usuario profesor;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Sector sector;

	@Column(nullable = false)
	private Integer cupoMaximo;

	@Column(nullable = false)
	private Boolean activa;

	@PrePersist
	void prePersist() {
		if (cupoMaximo == null) {
			cupoMaximo = 20;
		}
		if (activa == null) {
			activa = true;
		}
	}
}
