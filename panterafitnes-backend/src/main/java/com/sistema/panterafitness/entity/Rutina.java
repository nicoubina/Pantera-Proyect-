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
@Table(name = "rutinas")
public class Rutina {
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cliente_id", nullable = false)
  private Usuario cliente;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoRutina estado;

  @OneToMany(mappedBy = "rutina", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("orden ASC")
  @Builder.Default
  private java.util.List<RutinaEjercicio> ejercicios = new java.util.ArrayList<>();
}
