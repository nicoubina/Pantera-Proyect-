import { apiRequest } from "./apiClient";

export const modulesService = {
  list(resource, query = "") {
    return apiRequest(`/api/${resource}${query}`);
  },
  get(resource, id) {
    return apiRequest(`/api/${resource}/${id}`);
  },
  save(resource, id, body) {
    return apiRequest(`/api/${resource}${id ? `/${id}` : ""}`, {
      method: id ? "PUT" : "POST",
      body,
    });
  },
  remove(resource, id) {
    return apiRequest(`/api/${resource}/${id}`, { method: "DELETE" });
  },
  cancelPenalty(id) {
    return apiRequest(`/api/penalizaciones/${id}/cancelar`, {
      method: "PATCH",
    });
  },
  membership(id, body) {
    return apiRequest(`/api/usuarios/${id}/membresia`, {
      method: "PATCH",
      body,
    });
  },
};
