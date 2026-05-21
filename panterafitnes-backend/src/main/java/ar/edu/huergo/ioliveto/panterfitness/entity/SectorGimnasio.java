package ar.edu.huergo.ioliveto.panterfitness.entity;

import ar.edu.huergo.ioliveto.panterfitness.enums.Sector;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "sectores_gimnasio")
public class SectorGimnasio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, unique = true)
	private Sector nombre;

	@Column(nullable = false)
	private Integer capacidadMaxima;

	@Column(nullable = false)
	private Integer ocupacionActual;

	@Column(nullable = false)
	private Boolean activo;

	@PrePersist
	void prePersist() {
		if (ocupacionActual == null) {
			ocupacionActual = 0;
		}
		if (activo == null) {
			activo = true;
		}
	}
}
