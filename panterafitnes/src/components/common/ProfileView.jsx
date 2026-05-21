"use client";

import PageHeader from "@/components/common/PageHeader";
import StatusPill from "@/components/common/StatusPill";
import NotificationsList from "@/components/notificaciones/NotificationsList";
import QrSimulator from "@/components/qr/QrSimulator";
import { getRoleLabel } from "@/components/layout/navigation";
import { ROLES } from "@/data/mockUsers";
import { useAuth } from "@/context/AuthContext";

export default function ProfileView() {
  const { user } = useAuth();

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Perfil"
        title={user.nombre}
        description="Datos de sesion mockeados y persistidos en localStorage."
      />

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
                  <StatusPill tone={user.membresia === "ACTIVA" ? "success" : "danger"}>
                    {user.membresia}
                  </StatusPill>
                </dd>
              </div>
            ) : null}
          </dl>
        </article>
        <QrSimulator />
      </section>

      <section className="panel">
        <div className="section-title-row">
          <div>
            <h3>Notificaciones internas</h3>
            <p className="muted">No se envian emails ni WhatsApp reales.</p>
          </div>
        </div>
        <NotificationsList />
      </section>
    </div>
  );
}
