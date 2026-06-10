"use client";

import Link from "next/link";
import MetricCard from "@/components/common/MetricCard";
import PageHeader from "@/components/common/PageHeader";
import StatusPill from "@/components/common/StatusPill";
import { ROLES } from "@/data/mockUsers";
import { authService } from "@/services/authService";
import { getClassAvailability } from "@/services/classService";
import { RESERVA_ESTADOS } from "@/services/reservationService";
import { useAppData } from "@/context/AppDataContext";
import { useAuth } from "@/context/AuthContext";

function formatClass(classItem) {
  return `${classItem.nombre} - ${classItem.diaNombre} ${classItem.hora}`;
}

export default function DashboardHome({ role }) {
  const { user } = useAuth();
  const { classes, reservations, occupancy, notifications } = useAppData();
  const unreadCount = notifications.filter((item) => !item.leida).length;

  if (role === ROLES.CLIENTE) {
    const myReservations = reservations.filter((reservation) => reservation.userId === user.id);
    const nextReservation = myReservations.find(
      (reservation) => reservation.estado === RESERVA_ESTADOS.CONFIRMADA
    );
    const nextClass = nextReservation
      ? classes.find((classItem) => classItem.id === nextReservation.classId)
      : null;
    const waitingCount = myReservations.filter(
      (reservation) => reservation.estado === RESERVA_ESTADOS.EN_ESPERA
    ).length;

    const isMembershipExpired = user.membresia !== "ACTIVA";

    return (
      <div className="stack">
        {isMembershipExpired ? (
          <div
            style={{
              position: "sticky",
              top: 0,
              zIndex: 30,
              display: "flex",
              alignItems: "center",
              gap: 12,
              border: "1px solid var(--color-red)",
              background: "var(--color-red-bg)",
              borderRadius: 8,
              padding: "12px 16px",
              color: "#ffd5d5"
            }}
          >
            <span className="material-symbols-outlined">warning</span>
            <span style={{ flex: 1 }}>
              Tu membresía está {user.membresia.toLowerCase()}. Renovala para poder reservar clases.
            </span>
            <Link className="ghost-button small link-button" href="/cliente/perfil">
              Ver perfil
            </Link>
          </div>
        ) : null}

        <section
          className="dashboard-hero card-glow"
          style={{
            display: "grid",
            gridTemplateColumns: "1fr 1fr",
            gap: 24,
            background: "var(--color-surface)",
            border: "1px solid var(--color-border)",
            borderRadius: 8,
            padding: "32px",
            position: "relative",
            overflow: "hidden"
          }}
        >
          <div>
            <div style={{ marginBottom: 16 }}>
              <StatusPill tone={user.membresia === "ACTIVA" ? "success" : "danger"}>
                <span className={`status-dot ${user.membresia === "ACTIVA" ? "green" : "red"}`} />
                {" "}
                {user.membresia}
              </StatusPill>
            </div>
            <h2 className="font-display" style={{ fontSize: "2.2rem" }}>
              Hola, {user.nombre}
            </h2>
            <p className="muted" style={{ marginTop: 12 }}>
              <span
                className="material-symbols-outlined"
                style={{ fontSize: 18, verticalAlign: "middle", color: "var(--color-orange)" }}
              >
                event_available
              </span>{" "}
              Tenés <strong>{nextClass ? "1 reserva" : "0 reservas"}</strong> confirmadas esta semana
            </p>
          </div>
          <div
            style={{
              background: "var(--color-surface-2)",
              border: "1px solid var(--color-border)",
              borderRadius: 8,
              padding: 20
            }}
          >
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", marginBottom: 16 }}>
              <div>
                <p className="eyebrow">Ocupación actual</p>
                <p style={{ fontWeight: 800 }}>Sede Central</p>
              </div>
              <strong style={{ fontSize: "2rem", color: "var(--color-orange)" }}>
                {occupancy?.total.porcentaje ?? 0}%
              </strong>
            </div>
            <div className="progress-track" style={{ height: 12 }}>
              <span className="occupancy-bar" style={{ width: `${occupancy?.total.porcentaje ?? 0}%` }} />
            </div>
            <p className="muted" style={{ marginTop: 8, fontSize: "0.85rem" }}>
              {occupancy?.total.estado}
            </p>
          </div>
        </section>

        <section className="metric-grid">
          <MetricCard
            icon="shield"
            label="Membresia"
            value={user.membresia}
            detail={user.membresia === "ACTIVA" ? "Habilitado para reservar" : "Reserva bloqueada"}
            tone={user.membresia === "ACTIVA" ? "success" : "danger"}
          />
          <MetricCard
            icon="calendar_month"
            label="Proxima reserva"
            value={nextClass ? nextClass.nombre : "Sin reserva"}
            detail={nextClass ? `${nextClass.diaNombre} ${nextClass.hora}` : "Reserva desde Clases"}
          />
          <MetricCard
            icon="hourglass_empty"
            label="Lista de espera"
            value={waitingCount}
            detail="Reservas pendientes por cupo"
          />
          <MetricCard
            icon="notifications"
            label="Notificaciones"
            value={unreadCount}
            detail="Internas y simuladas"
          />
        </section>

        <section className="card-grid">
          <Link
            className="panel link-button card-glow"
            href="/cliente/clases"
            style={{ display: "flex", alignItems: "center", gap: 16, minHeight: 56, textDecoration: "none" }}
          >
            <span className="material-symbols-outlined" style={{ fontSize: 28, color: "var(--color-orange)" }}>
              event
            </span>
            <span style={{ flex: 1 }}>
              <strong style={{ display: "block" }}>Reservar clase</strong>
              <span className="muted" style={{ fontSize: "0.85rem" }}>
                Mira la grilla semanal y reservá tu lugar
              </span>
            </span>
            <span className="material-symbols-outlined muted">chevron_right</span>
          </Link>
          <Link
            className="panel link-button card-glow"
            href="/cliente/perfil"
            style={{ display: "flex", alignItems: "center", gap: 16, minHeight: 56, textDecoration: "none" }}
          >
            <span className="material-symbols-outlined" style={{ fontSize: 28, color: "var(--color-orange)" }}>
              qr_code
            </span>
            <span style={{ flex: 1 }}>
              <strong style={{ display: "block" }}>Ver QR simulado</strong>
              <span className="muted" style={{ fontSize: "0.85rem" }}>
                Tu credencial de acceso al gimnasio
              </span>
            </span>
            <span className="material-symbols-outlined muted">chevron_right</span>
          </Link>
        </section>
      </div>
    );
  }

  if (role === ROLES.PROFESOR) {
    const assignedClasses = classes.filter((classItem) => classItem.profesorId === user.id);
    const assignedIds = assignedClasses.map((classItem) => classItem.id);
    const professorReservations = reservations.filter(
      (reservation) =>
        assignedIds.includes(reservation.classId) &&
        reservation.estado !== RESERVA_ESTADOS.CANCELADA
    );

    return (
      <div className="stack">
        <PageHeader
          eyebrow="Inicio profesor"
          title="Clases asignadas y asistencia simulada"
          description="Vista preparada para seguimiento de cupos, alumnos y check-in."
        />
        <section className="metric-grid">
          <MetricCard icon="fitness_center" label="Clases asignadas" value={assignedClasses.length} detail="Semana actual" />
          <MetricCard icon="groups" label="Alumnos inscriptos" value={professorReservations.length} detail="Confirmados y espera" />
          <MetricCard icon="qr_code" label="Check-in" value="Simulado" detail="Pantalla preparada para QR" />
          <MetricCard icon="monitoring" label="Ocupacion gimnasio" value={occupancy ? `${occupancy.total.porcentaje}%` : "..."} detail="Actualiza cada 10 segundos" />
        </section>
        <section className="panel">
          <h3>Proximas clases</h3>
          <div className="card-grid">
            {assignedClasses.slice(0, 4).map((classItem) => {
              const availability = getClassAvailability(classItem);
              return (
                <article className="mini-card" key={classItem.id}>
                  <h4>{formatClass(classItem)}</h4>
                  <p>
                    {availability.estado} - {classItem.cuposOcupados}/{classItem.cupoTotal} cupos
                    ocupados
                  </p>
                </article>
              );
            })}
          </div>
        </section>
      </div>
    );
  }

  const users = authService.getAllUsers();
  const activeUsers = users.filter((item) => item.membresia === "ACTIVA").length;
  const expiredUsers = users.filter((item) => item.membresia === "VENCIDA").length;
  const confirmedToday = reservations.filter(
    (reservation) => reservation.estado === RESERVA_ESTADOS.CONFIRMADA
  ).length;
  const topClass = [...classes].sort((a, b) => b.cuposOcupados - a.cuposOcupados)[0];
  const topClasses = [...classes]
    .sort((a, b) => b.cuposOcupados / b.cupoTotal - a.cuposOcupados / a.cupoTotal)
    .slice(0, 4);
  const today = new Intl.DateTimeFormat("es-AR", { dateStyle: "full" }).format(new Date());

  function nivelTone(nivel) {
    if (nivel === "Alta ocupacion") {
      return "danger";
    }

    if (nivel === "Media ocupacion") {
      return "warning";
    }

    return "success";
  }

  return (
    <div className="stack">
      <p className="eyebrow">{today}</p>
      <PageHeader
        title="Resumen operativo simulado"
        description="Metricas basicas del MVP con datos mockeados en frontend."
      />
      <section className="metric-grid">
        <MetricCard icon="calendar_month" label="Reservas del dia" value={confirmedToday} detail="Confirmadas simuladas" />
        <MetricCard icon="monitoring" label="Ocupacion promedio" value={occupancy ? `${occupancy.total.porcentaje}%` : "..."} detail={occupancy?.total.estado} />
        <MetricCard icon="groups" label="Usuarios activos" value={activeUsers} detail={`${expiredUsers} membresias vencidas`} />
        <MetricCard icon="star" label="Clase mas ocupada" value={topClass?.nombre || "-"} detail={topClass ? `${topClass.cuposOcupados}/${topClass.cupoTotal} cupos` : ""} />
      </section>
      <section className="panel">
        <h3>Semáforo de clases</h3>
        <div style={{ display: "grid", gap: 12 }}>
          {topClasses.map((classItem) => {
            const availability = getClassAvailability(classItem);
            return (
              <div
                key={classItem.id}
                style={{ display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap" }}
              >
                <div style={{ minWidth: 160 }}>
                  <strong style={{ display: "block" }}>{classItem.nombre}</strong>
                  <span className="muted" style={{ fontSize: "0.8rem" }}>
                    {classItem.diaNombre} {classItem.hora}
                  </span>
                </div>
                <div className="progress-track" style={{ flex: 1, minWidth: 100 }}>
                  <span style={{ width: `${availability.porcentaje}%` }} />
                </div>
                <StatusPill tone={nivelTone(availability.nivel)}>{availability.nivel}</StatusPill>
              </div>
            );
          })}
        </div>
      </section>
      <section className="panel">
        <h3>Estado de membresias</h3>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
          <div style={{ padding: 20, background: "var(--color-green-bg)", border: "1px solid var(--color-green)", borderRadius: 8 }}>
            <p style={{ fontSize: "0.72rem", fontWeight: 800, textTransform: "uppercase", letterSpacing: "0.1em", color: "var(--color-green)" }}>
              Activas
            </p>
            <strong style={{ fontSize: "2.2rem", color: "var(--color-green)" }}>{activeUsers}</strong>
          </div>
          <div style={{ padding: 20, background: "var(--color-red-bg)", border: "1px solid var(--color-red)", borderRadius: 8 }}>
            <p style={{ fontSize: "0.72rem", fontWeight: 800, textTransform: "uppercase", letterSpacing: "0.1em", color: "var(--color-red)" }}>
              Vencidas
            </p>
            <strong style={{ fontSize: "2.2rem", color: "var(--color-red)" }}>{expiredUsers}</strong>
          </div>
        </div>
      </section>
    </div>
  );
}
