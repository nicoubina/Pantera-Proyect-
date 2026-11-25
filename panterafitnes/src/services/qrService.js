import { apiRequest } from "./apiClient";

export const qrService = {
  /** Recepción o el profesor registran el ingreso del socio con el código leído de su QR. */
  registerEntry({ codigo, horarioClaseId }) {
    return apiRequest("/api/qr/ingreso", {
      method: "POST", body: { codigo, horarioClaseId: horarioClaseId ?? null }
    });
  }
};
