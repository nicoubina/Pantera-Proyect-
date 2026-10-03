package com.sistema.panterafitness.entity;

import com.sistema.panterafitness.enums.*;
import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "penalizaciones")
public class Penalizacion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @Column(nullable = false, length = 1000)
  private String motivo;

  @Column(nullable = false)
  private LocalDate fechaInicio;

  @Column(nullable = false)
  private LocalDate fechaFin;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoPenalizacion estado;
}
