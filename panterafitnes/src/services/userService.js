import { apiRequest } from "./apiClient";
import { mapUser } from "./mappers";

export const userService = {
  async getAllUsers() { return (await apiRequest("/api/usuarios")).map(mapUser); },
  async updateMembership(id, membership) {
    return mapUser(await apiRequest(`/api/usuarios/${id}/membresia`, { method: "PATCH", body: membership }));
  }
};
