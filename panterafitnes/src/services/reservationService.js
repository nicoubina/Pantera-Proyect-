import { apiRequest } from "./apiClient";
import { mapReservation } from "./mappers";

export const RESERVA_ESTADOS = {
  CONFIRMADA: "CONFIRMADA", CANCELADA: "CANCELADA", EN_ESPERA: "EN_ESPERA",
  ASISTIDA: "ASISTIDA", AUSENTE: "AUSENTE"
};

// Only controls button visibility; the backend decides whether cancellation is allowed.
export function isReservationCancelable(reservation) {
  return [RESERVA_ESTADOS.CONFIRMADA, RESERVA_ESTADOS.EN_ESPERA].includes(reservation?.estado);
}

export const reservationService = {
  async getReservations(role) {
    const path = role === "CLIENTE" ? "/api/reservas/mis-reservas" : "/api/reservas";
    return (await apiRequest(path)).map(mapReservation);
  },
  async createReservation(classId) {
    return mapReservation(await apiRequest("/api/reservas", {
      method: "POST", body: { horarioClaseId: classId }
    }));
  },
  cancelReservation(id) { return apiRequest(`/api/reservas/${id}/cancelar`, { method: "DELETE" }); }
};
