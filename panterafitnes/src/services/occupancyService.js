import { initialOccupancy } from "@/data/mockOccupancy";
import { getOccupationLevel, getOccupationPercent } from "@/services/classService";
import { readStorage, writeStorage } from "@/services/storageService";

const OCCUPANCY_KEY = "pantera_occupancy";

function clamp(value, min, max) {
  return Math.min(Math.max(value, min), max);
}

function randomDelta() {
  return Math.floor(Math.random() * 5) - 2;
}

export function enrichOccupancyItem(item) {
  const porcentaje = getOccupationPercent(item.personas, item.capacidad);

  return {
    ...item,
    porcentaje,
    estado: getOccupationLevel(porcentaje)
  };
}

export const occupancyService = {
  getCurrentOccupancy() {
    const stored = readStorage(OCCUPANCY_KEY, null);
    const occupancy = stored || initialOccupancy;
    return {
      ...occupancy,
      total: enrichOccupancyItem(occupancy.total),
      sectores: occupancy.sectores.map(enrichOccupancyItem)
    };
  },

  simulateNextOccupancy(currentOccupancy) {
    const sectores = currentOccupancy.sectores.map((sector) => ({
      ...sector,
      personas: clamp(sector.personas + randomDelta(), 0, sector.capacidad)
    }));
    const totalPeople = sectores.reduce((sum, sector) => sum + sector.personas, 0);
    const nextOccupancy = {
      updatedAt: new Date().toISOString(),
      total: {
        ...currentOccupancy.total,
        personas: clamp(totalPeople, 0, currentOccupancy.total.capacidad)
      },
      sectores
    };

    writeStorage(OCCUPANCY_KEY, nextOccupancy);
    return {
      ...nextOccupancy,
      total: enrichOccupancyItem(nextOccupancy.total),
      sectores: nextOccupancy.sectores.map(enrichOccupancyItem)
    };
  }
};
