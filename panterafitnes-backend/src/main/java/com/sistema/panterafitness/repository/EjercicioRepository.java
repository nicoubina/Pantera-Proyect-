package com.sistema.panterafitness.repository;

import com.sistema.panterafitness.entity.Ejercicio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EjercicioRepository extends JpaRepository<Ejercicio, Long> {
  boolean existsByNombreIgnoreCase(String nombre);
}
