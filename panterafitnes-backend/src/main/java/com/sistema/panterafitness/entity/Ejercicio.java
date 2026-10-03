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
@Table(name = "ejercicios")
public class Ejercicio {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String nombre;

  @Column(length = 1000)
  private String descripcion;
}
