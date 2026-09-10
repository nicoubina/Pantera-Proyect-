import { apiRequest } from "./apiClient";
import { mapOccupancy } from "./mappers";

export const occupancyService = {
  getClassOccupancy(id) { return apiRequest(`/api/ocupacion/clases/${id}`); },
  getClassesOccupancy() { return apiRequest("/api/ocupacion/clases"); },
  async getCurrentOccupancy() {
    const [general, sectores] = await Promise.all([
      apiRequest("/api/ocupacion/general"), apiRequest("/api/ocupacion/sectores")
    ]);
    return {
      updatedAt: new Date().toISOString(), total: mapOccupancy(general, "Gimnasio"),
      sectores: sectores.map((sector) => mapOccupancy(sector,
        { MUSCULACION: "Musculación", SALA_CLASES: "Sala de clases" }[sector.sector] || sector.sector))
    };
  }
};
