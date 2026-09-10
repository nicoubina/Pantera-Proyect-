const fullName = (user) => [user?.nombre, user?.apellido].filter(Boolean).join(" ");
const minutes = (time) => Number(time.slice(0, 2)) * 60 + Number(time.slice(3, 5));

export function mapUser(dto) {
  return {
    id: dto.id, nombre: fullName(dto), email: dto.email, rol: dto.rol,
    membresia: dto.estadoMembresia, activo: dto.activo, qrSimulado: dto.qrSimulado,
    fechaInicioMembresia: dto.fechaInicioMembresia,
    fechaVencimientoMembresia: dto.fechaVencimientoMembresia
  };
}

export function mapSchedule(dto, occupancy) {
  const clase = dto.claseGimnasio;
  return {
    id: dto.id, claseId: clase.id, nombre: clase.nombre, descripcion: clase.descripcion,
    profesorId: clase.profesor.id, profesor: fullName(clase.profesor), sector: clase.sector,
    fecha: dto.fecha, hora: dto.horaInicio.slice(0, 5), horaFin: dto.horaFin.slice(0, 5),
    diaNombre: new Date(`${dto.fecha}T12:00:00`).toLocaleDateString("es-AR", { weekday: "long" }),
    duracionMinutos: minutes(dto.horaFin) - minutes(dto.horaInicio),
    cupoTotal: occupancy?.capacidadMaxima ?? dto.cupoMaximo,
    cuposOcupados: occupancy?.ocupacionActual ?? 0, activa: dto.activa
  };
}

export function mapReservation(dto) {
  return {
    id: dto.id, userId: dto.usuario.id, userName: fullName(dto.usuario),
    classId: dto.horarioClase.id, classItem: mapSchedule(dto.horarioClase),
    estado: dto.estadoReserva, createdAt: dto.fechaCreacion,
    posicionListaEspera: dto.posicionListaEspera
  };
}

export function mapOccupancy(dto, nombre) {
  return {
    id: dto.id, nombre, personas: dto.ocupacionActual, capacidad: dto.capacidadMaxima,
    porcentaje: Math.round(dto.porcentajeOcupacion), estado: { BAJA: "Baja ocupacion", MEDIA: "Media ocupacion", ALTA: "Alta ocupacion" }[dto.estado] || dto.estado
  };
}

export function mapNotification(dto) {
  return {
    id: dto.id, titulo: dto.titulo, mensaje: dto.mensaje,
    tipo: dto.tipoNotificacion, leida: dto.estadoNotificacion === "LEIDA", fecha: dto.fechaCreacion
  };
}
