"use client";
import PageHeader from "@/components/common/PageHeader";
import NotificationsList from "@/components/notificaciones/NotificationsList";
import { useAppData } from "@/context/AppDataContext";
export default function NotificationsView() {
  const { markAllNotificationsAsRead, pending, notifications } = useAppData();
  return (
    <div className="stack">
      <PageHeader
        title="Notificaciones"
        description="Mensajes internos sobre reservas, rutinas y novedades del gimnasio."
      />
      <button
        className="secondary-button"
        disabled={pending || !notifications.some((n) => !n.leida)}
        onClick={markAllNotificationsAsRead}
      >
        Marcar todas como leídas
      </button>
      <NotificationsList />
    </div>
  );
}
