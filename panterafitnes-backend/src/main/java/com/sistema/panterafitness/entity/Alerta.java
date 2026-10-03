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
@Table(name = "alertas")
public class Alerta {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String titulo;

  @Column(nullable = false, length = 1000)
  private String descripcion;

  @Column(nullable = false, updatable = false)
  private LocalDateTime fechaCreacion;

  @Column(nullable = false)
  private Boolean activa;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PrioridadAlerta prioridad;

  @PrePersist
  void prePersist() {
    if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
  }
}
