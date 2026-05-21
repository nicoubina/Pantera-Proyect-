import { buildMockClasses, getRollingWeekKey } from "@/data/mockClasses";
import { readStorage, writeStorage } from "@/services/storageService";

const CLASSES_KEY = "pantera_weekly_classes";

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
  if (percent >= 85) {
    return "Alta ocupacion";
  }

  if (percent >= 55) {
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
  getWeeklyClasses() {
    const weekKey = getRollingWeekKey();
    const stored = readStorage(CLASSES_KEY, null);

    if (stored?.weekKey === weekKey && Array.isArray(stored.classes)) {
      return stored.classes;
    }

    const classes = buildMockClasses();
    writeStorage(CLASSES_KEY, { weekKey, classes });
    return classes;
  },

  saveWeeklyClasses(classes) {
    writeStorage(CLASSES_KEY, {
      weekKey: getRollingWeekKey(),
      classes
    });
  }
};
