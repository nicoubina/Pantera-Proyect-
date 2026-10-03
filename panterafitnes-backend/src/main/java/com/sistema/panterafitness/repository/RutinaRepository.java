package com.sistema.panterafitness.repository;

import com.sistema.panterafitness.entity.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RutinaRepository extends JpaRepository<Rutina, Long> {
  java.util.List<Rutina> findByProfesorIdOrderByIdDesc(Long profesorId);

  java.util.List<Rutina> findByClienteIdOrderByIdDesc(Long clienteId);
}
