package ar.edu.huergo.ioliveto.panterfitness.service;

import ar.edu.huergo.ioliveto.panterfitness.dto.ClaseRequest;
import ar.edu.huergo.ioliveto.panterfitness.dto.ClaseResponse;
import ar.edu.huergo.ioliveto.panterfitness.entity.ClaseGimnasio;
import ar.edu.huergo.ioliveto.panterfitness.entity.Usuario;
import ar.edu.huergo.ioliveto.panterfitness.enums.Rol;
import ar.edu.huergo.ioliveto.panterfitness.exception.BusinessException;
import ar.edu.huergo.ioliveto.panterfitness.exception.ResourceNotFoundException;
import ar.edu.huergo.ioliveto.panterfitness.mapper.EntityMapper;
import ar.edu.huergo.ioliveto.panterfitness.repository.ClaseGimnasioRepository;
import ar.edu.huergo.ioliveto.panterfitness.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClaseService {

	private final ClaseGimnasioRepository claseGimnasioRepository;
	private final UsuarioRepository usuarioRepository;
	private final UsuarioService usuarioService;

	@Transactional(readOnly = true)
	public List<ClaseResponse> listarVisibles() {
		Usuario actual = usuarioService.obtenerUsuarioAutenticado();
		List<ClaseGimnasio> clases = actual.getRol() == Rol.PROFESOR
				? claseGimnasioRepository.findByProfesorIdAndActivaTrueOrderByNombreAsc(actual.getId())
				: claseGimnasioRepository.findByActivaTrueOrderByNombreAsc();
		return clases.stream().map(EntityMapper::toClaseResponse).toList();
	}

	@Transactional(readOnly = true)
	public ClaseResponse obtenerPorId(Long id) {
		return EntityMapper.toClaseResponse(obtenerEntidad(id));
	}

	@Transactional
	public ClaseResponse crear(ClaseRequest request) {
		Usuario profesor = obtenerProfesor(request.profesorId());
		ClaseGimnasio clase = ClaseGimnasio.builder()
				.nombre(request.nombre().trim())
				.descripcion(request.descripcion())
				.profesor(profesor)
				.sector(request.sector())
				.cupoMaximo(request.cupoMaximo() == null ? 20 : request.cupoMaximo())
				.activa(request.activa() == null || request.activa())
				.build();
		return EntityMapper.toClaseResponse(claseGimnasioRepository.save(clase));
	}

	@Transactional
	public ClaseResponse actualizar(Long id, ClaseRequest request) {
		ClaseGimnasio clase = obtenerEntidad(id);
		Usuario profesor = obtenerProfesor(request.profesorId());
		clase.setNombre(request.nombre().trim());
		clase.setDescripcion(request.descripcion());
		clase.setProfesor(profesor);
		clase.setSector(request.sector());
		clase.setCupoMaximo(request.cupoMaximo() == null ? 20 : request.cupoMaximo());
		clase.setActiva(request.activa() == null || request.activa());
		return EntityMapper.toClaseResponse(clase);
	}

	@Transactional
	public void eliminar(Long id) {
		ClaseGimnasio clase = obtenerEntidad(id);
		clase.setActiva(false);
	}

	@Transactional(readOnly = true)
	public ClaseGimnasio obtenerEntidad(Long id) {
		return claseGimnasioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Clase no encontrada."));
	}

	private Usuario obtenerProfesor(Long profesorId) {
		Usuario profesor = usuarioRepository.findById(profesorId)
				.orElseThrow(() -> new ResourceNotFoundException("Profesor no encontrado."));
		if (profesor.getRol() != Rol.PROFESOR) {
			throw new BusinessException("El usuario asignado debe tener rol PROFESOR.");
		}
		return profesor;
	}
}
