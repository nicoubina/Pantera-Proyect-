package com.sistema.panterafitness.repository;

import com.sistema.panterafitness.entity.ListaEspera;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListaEsperaRepository extends JpaRepository<ListaEspera, Long> {

	List<ListaEspera> findByHorarioClaseIdAndActivaTrueOrderByFechaIngresoAsc(Long horarioClaseId);

	Optional<ListaEspera> findByUsuarioIdAndHorarioClaseIdAndActivaTrue(Long usuarioId, Long horarioClaseId);

	long countByHorarioClaseIdAndActivaTrue(Long horarioClaseId);
}
