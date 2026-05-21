"use client";

import EmptyState from "@/components/common/EmptyState";
import StatusPill from "@/components/common/StatusPill";
import { useAppData } from "@/context/AppDataContext";

function formatDate(value) {
  return new Intl.DateTimeFormat("es-AR", {
    dateStyle: "short",
    timeStyle: "short"
  }).format(new Date(value));
}

export default function NotificationsList() {
  const { notifications } = useAppData();

  if (!notifications.length) {
    return (
      <EmptyState
        title="Sin notificaciones"
        description="Las acciones de reserva, espera, cancelacion y QR generan mensajes internos."
      />
    );
  }

  return (
    <section className="notification-page-list">
      {notifications.map((notification) => (
        <article className="notification-row" key={notification.id}>
          <div>
            <div className="card-title-row">
              <h3>{notification.titulo}</h3>
              <StatusPill tone={notification.leida ? "neutral" : "warning"}>
                {notification.leida ? "Leida" : "No leida"}
              </StatusPill>
            </div>
            <p>{notification.mensaje}</p>
            <span className="muted">{formatDate(notification.fecha)}</span>
          </div>
        </article>
      ))}
    </section>
  );
}
