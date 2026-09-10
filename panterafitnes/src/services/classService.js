import { apiRequest } from "./apiClient";
import { mapSchedule } from "./mappers";

export function getClassDateTime(classItem) {
  return new Date(`${classItem.fecha}T${classItem.hora}:00`);
}

export function getClassEndDateTime(classItem) {
  const start = getClassDateTime(classItem);
  return new Date(start.getTime() + classItem.duracionMinutos * 60 * 1000);
}

export function getOccupationPercent(current, total) {
  if (!total) {
    return 0;
  }

  return Math.round((current / total) * 100);
}

export function getOccupationLevel(percent) {
  if (percent >= 80) {
    return "Alta ocupacion";
  }

  if (percent >= 50) {
    return "Media ocupacion";
  }

  return "Baja ocupacion";
}

export function getClassAvailability(classItem) {
  const disponibles = Math.max(classItem.cupoTotal - classItem.cuposOcupados, 0);
  const porcentaje = getOccupationPercent(classItem.cuposOcupados, classItem.cupoTotal);

  if (disponibles === 0) {
    return {
      cuposDisponibles: 0,
      porcentaje,
      nivel: getOccupationLevel(porcentaje),
      estado: "Completa"
    };
  }

  if (disponibles <= 3) {
    return {
      cuposDisponibles: disponibles,
      porcentaje,
      nivel: getOccupationLevel(porcentaje),
      estado: "Ultimos cupos"
    };
  }

  return {
    cuposDisponibles: disponibles,
    porcentaje,
    nivel: getOccupationLevel(porcentaje),
    estado: "Disponible"
  };
}

export const classService = {
  getClasses() { return apiRequest("/api/clases"); },
  getSchedules() { return apiRequest("/api/horarios"); },
  getWeeklySchedules() { return apiRequest("/api/horarios/semana"); },
  async getWeeklyClasses(occupancies = []) {
    const schedules = await apiRequest("/api/clases/semana");
    return Promise.all(schedules.map(async (dto) => {
      const occupancy = occupancies.find((item) => item.horarioId === dto.id)
        || await apiRequest(`/api/ocupacion/clases/${dto.id}`);
      return mapSchedule(dto, occupancy);
    }));
  }
};
