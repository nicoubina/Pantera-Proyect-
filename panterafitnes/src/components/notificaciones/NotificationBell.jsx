"use client";

import { useState } from "react";
import { useAppData } from "@/context/AppDataContext";

function formatDate(value) {
  return new Intl.DateTimeFormat("es-AR", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  }).format(new Date(value));
}

export default function NotificationBell() {
  const { notifications, markAllNotificationsAsRead } = useAppData();
  const [open, setOpen] = useState(false);
  const unreadCount = notifications.filter((notification) => !notification.leida).length;

  return (
    <div className="notification-shell">
      <button
        className="icon-button"
        type="button"
        aria-label="Abrir notificaciones"
        onClick={() => setOpen((current) => !current)}
      >
        <span className="bell-dot">Notificaciones</span>
        {unreadCount ? <span className="badge-count">{unreadCount}</span> : null}
      </button>

      {open ? (
        <section className="notification-panel">
          <div className="panel-header">
            <h2>Notificaciones</h2>
            <button className="ghost-button small" type="button" onClick={markAllNotificationsAsRead}>
              Marcar leidas
            </button>
          </div>

          {notifications.length ? (
            <div className="notification-list">
              {notifications.slice(0, 6).map((notification) => (
                <article
                  className={`notification-item ${notification.leida ? "read" : "unread"}`}
                  key={notification.id}
                >
                  <div>
                    <strong>{notification.titulo}</strong>
                    <p>{notification.mensaje}</p>
                  </div>
                  <span>{formatDate(notification.fecha)}</span>
                </article>
              ))}
            </div>
          ) : (
            <p className="muted">Todavia no hay notificaciones internas.</p>
          )}
        </section>
      ) : null}
    </div>
  );
}
