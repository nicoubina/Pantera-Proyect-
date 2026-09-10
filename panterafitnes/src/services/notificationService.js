import { apiRequest } from "./apiClient";
import { mapNotification } from "./mappers";

export const notificationService = {
  async getByUser() { return (await apiRequest("/api/notificaciones/mis-notificaciones")).map(mapNotification); },
  async markAsRead(id) {
    return mapNotification(await apiRequest(`/api/notificaciones/${id}/leer`, { method: "PATCH" }));
  },
  async markAllAsRead(notifications) {
    for (const item of notifications.filter((item) => !item.leida)) await this.markAsRead(item.id);
  }
};
