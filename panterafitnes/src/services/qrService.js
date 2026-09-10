import { apiRequest } from "./apiClient";

export const qrService = {
  simulateCheckIn({ qrSimulado, horarioClaseId, horaIngresoSimulada }) {
    return apiRequest("/api/qr/simular-ingreso", {
      method: "POST", body: { qrSimulado, horarioClaseId, horaIngresoSimulada }
    });
  }
};
