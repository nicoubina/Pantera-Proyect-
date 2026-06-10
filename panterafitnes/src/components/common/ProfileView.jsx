"use client";

import PageHeader from "@/components/common/PageHeader";
import StatusPill from "@/components/common/StatusPill";
import NotificationsList from "@/components/notificaciones/NotificationsList";
import QrSimulator from "@/components/qr/QrSimulator";
import { getRoleLabel } from "@/components/layout/navigation";
import { ROLES } from "@/data/mockUsers";
import { useAppData } from "@/context/AppDataContext";
import { useAuth } from "@/context/AuthContext";

function getInitials(name) {
  return name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("");
}

export default function ProfileView() {
  const { user } = useAuth();
  const { notifications } = useAppData();
  const unreadCount = notifications.filter((notification) => !notification.leida).length;
  const isActiveMembership = user.membresia === "ACTIVA";
  const membershipProgress = isActiveMembership ? 60 : 100;

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Perfil"
        title={user.nombre}
        description="Datos de sesion mockeados y persistidos en localStorage."
      />

      <section className="panel card-glow" style={{ display: "flex", alignItems: "center", gap: 16 }}>
        <span className="avatar-initials large">{getInitials(user.nombre)}</span>
        <div>
          <h3 className="font-display" style={{ marginBottom: 4 }}>{user.nombre}</h3>
          <p className="muted" style={{ marginBottom: 6 }}>{user.email}</p>
          <StatusPill tone="neutral">{getRoleLabel(user.rol)}</StatusPill>
        </div>
      </section>

      <section className="split-grid">
        <article className="panel">
          <h3>Datos del usuario</h3>
          <dl className="profile-list">
            <div>
              <dt>Email</dt>
              <dd>{user.email}</dd>
            </div>
            <div>
              <dt>Rol</dt>
              <dd>{getRoleLabel(user.rol)}</dd>
            </div>
            {user.rol === ROLES.CLIENTE ? (
              <div>
                <dt>Membresia</dt>
                <dd>
                  <StatusPill tone={isActiveMembership ? "success" : "danger"}>
                    {user.membresia}
                  </StatusPill>
                </dd>
              </div>
            ) : null}
          </dl>
          {user.rol === ROLES.CLIENTE ? (
            <div style={{ marginTop: 16 }}>
              <div className="card-title-row" style={{ marginBottom: 8 }}>
                <span className="muted" style={{ fontSize: "0.78rem" }}>Inicio del ciclo</span>
                <span className="muted" style={{ fontSize: "0.78rem" }}>Vencimiento</span>
              </div>
              <div className="timeline-bar">
                <span
                  className="timeline-bar-fill"
                  style={{
                    width: `${membershipProgress}%`,
                    background: isActiveMembership ? "var(--color-orange)" : "var(--color-red)"
                  }}
                />
                <span className="timeline-marker" style={{ left: `${membershipProgress}%` }} />
              </div>
              <p className="muted" style={{ marginTop: 8, fontSize: "0.78rem" }}>
                {isActiveMembership
                  ? "Tu membresia esta activa y dentro del ciclo mensual."
                  : "Tu membresia vencio. Renovala para reservar clases."}
              </p>
            </div>
          ) : null}
        </article>
        <QrSimulator />
      </section>

      <section className="panel">
        <div className="section-title-row">
          <div>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <h3>Notificaciones internas</h3>
              {unreadCount ? (
                <span className="status-pill warning">{unreadCount}</span>
              ) : null}
            </div>
            <p className="muted">No se envian emails ni WhatsApp reales.</p>
          </div>
        </div>
        <NotificationsList />
      </section>
    </div>
  );
}
