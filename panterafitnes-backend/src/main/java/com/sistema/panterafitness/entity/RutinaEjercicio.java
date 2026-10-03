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
@Table(name = "rutina_ejercicios")
public class RutinaEjercicio {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rutina_id", nullable = false)
  private Rutina rutina;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ejercicio_id", nullable = false)
  private Ejercicio ejercicio;

  @Column(nullable = false)
  private Integer series;

  @Column(nullable = false)
  private Integer repeticiones;

  @Column(precision = 10, scale = 2)
  private java.math.BigDecimal pesoSugerido;

  @Column(nullable = false)
  private Integer descanso;

  @Column(nullable = false)
  private Integer orden;
}
