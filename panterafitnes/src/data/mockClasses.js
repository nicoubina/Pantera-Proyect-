export const CAPACIDAD_CLASE = 20;

const PROFESOR = {
  id: "profesor-001",
  nombre: "Profesor Pantera"
};

const classTemplates = [
  {
    nombre: "Funcional",
    hora: "18:00",
    duracionMinutos: 60,
    profesorId: PROFESOR.id,
    profesor: PROFESOR.nombre
  },
  {
    nombre: "Musculacion",
    hora: "19:30",
    duracionMinutos: 60,
    profesorId: PROFESOR.id,
    profesor: PROFESOR.nombre
  }
];

const dayNames = [
  "Domingo",
  "Lunes",
  "Martes",
  "Miercoles",
  "Jueves",
  "Viernes",
  "Sabado"
];

function toDateKey(date) {
  return date.toISOString().slice(0, 10);
}

function addDays(date, amount) {
  const nextDate = new Date(date);
  nextDate.setDate(nextDate.getDate() + amount);
  return nextDate;
}

export function getRollingWeekKey(baseDate = new Date()) {
  const start = new Date(baseDate);
  start.setHours(0, 0, 0, 0);
  return toDateKey(start);
}

export function buildMockClasses(baseDate = new Date()) {
  const start = new Date(baseDate);
  start.setHours(0, 0, 0, 0);

  return Array.from({ length: 7 }).flatMap((_, dayIndex) => {
    const date = addDays(start, dayIndex);
    const fecha = toDateKey(date);
    const diaNombre = dayNames[date.getDay()];

    return classTemplates.map((template, templateIndex) => {
      const isFullExample = dayIndex === 2 && template.nombre === "Funcional";
      const isPeakExample = dayIndex === 3 && template.nombre === "Musculacion";
      const baseOccupied = isFullExample
        ? CAPACIDAD_CLASE
        : isPeakExample
          ? 18
          : 8 + ((dayIndex + templateIndex * 3) % 7);

      return {
        id: `${template.nombre.toLowerCase()}-${fecha}-${template.hora.replace(":", "")}`,
        nombre: template.nombre,
        profesorId: template.profesorId,
        profesor: template.profesor,
        fecha,
        diaNombre,
        hora: template.hora,
        duracionMinutos: template.duracionMinutos,
        cupoTotal: CAPACIDAD_CLASE,
        cuposOcupados: baseOccupied
      };
    });
  });
}
