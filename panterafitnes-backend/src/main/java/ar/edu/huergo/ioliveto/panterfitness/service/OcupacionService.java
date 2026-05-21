package ar.edu.huergo.ioliveto.panterfitness.service;

import ar.edu.huergo.ioliveto.panterfitness.dto.OcupacionClaseResponse;
import ar.edu.huergo.ioliveto.panterfitness.dto.OcupacionGeneralResponse;
import ar.edu.huergo.ioliveto.panterfitness.dto.OcupacionSectorResponse;
import ar.edu.huergo.ioliveto.panterfitness.entity.HorarioClase;
import ar.edu.huergo.ioliveto.panterfitness.entity.SectorGimnasio;
import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoReserva;
import ar.edu.huergo.ioliveto.panterfitness.exception.ResourceNotFoundException;
import ar.edu.huergo.ioliveto.panterfitness.repository.HorarioClaseRepository;
import ar.edu.huergo.ioliveto.panterfitness.repository.ReservaRepository;
import ar.edu.huergo.ioliveto.panterfitness.repository.SectorGimnasioRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OcupacionService {

	private static final int CAPACIDAD_TOTAL_GIMNASIO = 60;
	private static final List<EstadoReserva> ESTADOS_OCUPAN_CUPO = List.of(
			EstadoReserva.CONFIRMADA,
			EstadoReserva.ASISTIDA
	);

	private final SectorGimnasioRepository sectorGimnasioRepository;
	private final HorarioClaseRepository horarioClaseRepository;
	private final ReservaRepository reservaRepository;

	@Transactional(readOnly = true)
	public OcupacionGeneralResponse obtenerGeneral() {
		List<OcupacionSectorResponse> sectores = obtenerSectores();
		int ocupacionActual = sectores.stream()
				.mapToInt(OcupacionSectorResponse::ocupacionActual)
				.sum();
		return new OcupacionGeneralResponse(
				ocupacionActual,
				CAPACIDAD_TOTAL_GIMNASIO,
				calcularPorcentaje(ocupacionActual, CAPACIDAD_TOTAL_GIMNASIO),
				calcularEstado(ocupacionActual, CAPACIDAD_TOTAL_GIMNASIO),
				sectores
		);
	}

	@Transactional(readOnly = true)
	public List<OcupacionSectorResponse> obtenerSectores() {
		return sectorGimnasioRepository.findByActivoTrueOrderByNombreAsc()
				.stream()
				.map(this::toSectorResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<OcupacionClaseResponse> obtenerClases() {
		LocalDate desde = LocalDate.now();
		LocalDate hasta = desde.plusDays(7);
		return horarioClaseRepository.findByActivaTrueAndFechaBetweenOrderByFechaAscHoraInicioAsc(desde, hasta)
				.stream()
				.map(this::toClaseResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public OcupacionClaseResponse obtenerClase(Long horarioId) {
		HorarioClase horario = horarioClaseRepository.findById(horarioId)
				.orElseThrow(() -> new ResourceNotFoundException("Horario de clase no encontrado."));
		return toClaseResponse(horario);
	}

	private OcupacionSectorResponse toSectorResponse(SectorGimnasio sector) {
		return new OcupacionSectorResponse(
				sector.getId(),
				sector.getNombre(),
				sector.getOcupacionActual(),
				sector.getCapacidadMaxima(),
				calcularPorcentaje(sector.getOcupacionActual(), sector.getCapacidadMaxima()),
				calcularEstado(sector.getOcupacionActual(), sector.getCapacidadMaxima())
		);
	}

	private OcupacionClaseResponse toClaseResponse(HorarioClase horario) {
		int ocupacion = (int) reservaRepository.countByHorarioClaseIdAndEstadoReservaIn(
				horario.getId(),
				ESTADOS_OCUPAN_CUPO
		);
		int capacidad = horario.getCupoMaximo();
		return new OcupacionClaseResponse(
				horario.getId(),
				horario.getClaseGimnasio().getId(),
				horario.getClaseGimnasio().getNombre(),
				horario.getDiaSemana(),
				horario.getFecha(),
				horario.getHoraInicio(),
				horario.getHoraFin(),
				ocupacion,
				capacidad,
				calcularPorcentaje(ocupacion, capacidad),
				calcularEstado(ocupacion, capacidad)
		);
	}

	private double calcularPorcentaje(int ocupacionActual, int capacidadMaxima) {
		if (capacidadMaxima <= 0) {
			return 0.0;
		}
		return Math.round((ocupacionActual * 10000.0) / capacidadMaxima) / 100.0;
	}

	private String calcularEstado(int ocupacionActual, int capacidadMaxima) {
		double porcentaje = calcularPorcentaje(ocupacionActual, capacidadMaxima);
		if (porcentaje < 50.0) {
			return "BAJA";
		}
		if (porcentaje < 80.0) {
			return "MEDIA";
		}
		return "ALTA";
	}
}
