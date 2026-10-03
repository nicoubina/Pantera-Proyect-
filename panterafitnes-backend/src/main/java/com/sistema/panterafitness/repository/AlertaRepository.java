package com.sistema.panterafitness.repository;

import com.sistema.panterafitness.entity.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
  java.util.List<Alerta> findByActivaTrueOrderByFechaCreacionDesc();
}
