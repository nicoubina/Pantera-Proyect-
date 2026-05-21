import { readStorage, writeStorage } from "@/services/storageService";

const NOTIFICATIONS_KEY = "pantera_notifications";

export const notificationService = {
  getAll() {
    return readStorage(NOTIFICATIONS_KEY, []);
  },

  getByUser(userId) {
    return this.getAll().filter((notification) => notification.userId === userId);
  },

  createNotification({ userId, titulo, mensaje, tipo = "INFO" }) {
    const notifications = this.getAll();
    const newNotification = {
      id: `notif-${Date.now()}-${Math.random().toString(16).slice(2)}`,
      userId,
      titulo,
      mensaje,
      tipo,
      leida: false,
      fecha: new Date().toISOString()
    };

    const nextNotifications = [newNotification, ...notifications];
    writeStorage(NOTIFICATIONS_KEY, nextNotifications);
    return nextNotifications;
  },

  markAllAsRead(userId) {
    const nextNotifications = this.getAll().map((notification) =>
      notification.userId === userId ? { ...notification, leida: true } : notification
    );
    writeStorage(NOTIFICATIONS_KEY, nextNotifications);
    return nextNotifications;
  }
};
