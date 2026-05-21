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

    return (
      <div className="stack">
        <PageHeader
          eyebrow="Inicio cliente"
          title={`Hola, ${user.nombre}`}
          description="Gestiona reservas, ocupacion y QR simulado desde el frontend."
        />
        <section className="metric-grid">
          <MetricCard
            label="Membresia"
            value={user.membresia}
            detail={user.membresia === "ACTIVA" ? "Habilitado para reservar" : "Reserva bloqueada"}
            tone={user.membresia === "ACTIVA" ? "success" : "danger"}
          />
          <MetricCard
            label="Proxima reserva"
            value={nextClass ? nextClass.nombre : "Sin reserva"}
            detail={nextClass ? `${nextClass.diaNombre} ${nextClass.hora}` : "Reserva desde Clases"}
          />
          <MetricCard
            label="Lista de espera"
            value={waitingCount}
            detail="Reservas pendientes por cupo"
          />
          <MetricCard
            label="Notificaciones"
            value={unreadCount}
            detail="Internas y simuladas"
          />
        </section>
        <section className="split-grid">
          <article className="panel">
            <h3>Accesos rapidos</h3>
            <div className="actions-row">
              <Link className="primary-button link-button" href="/cliente/clases">
                Reservar clase
              </Link>
              <Link className="ghost-button link-button" href="/cliente/perfil">
                Ver QR simulado
              </Link>
            </div>
          </article>
          <article className="panel">
            <h3>Ocupacion general</h3>
            {occupancy ? (
              <div className="occupancy-summary">
                <strong>{occupancy.total.porcentaje}%</strong>
                <p>
                  {occupancy.total.estado} - {occupancy.total.personas}/
                  {occupancy.total.capacidad} personas
                </p>
              </div>
            ) : (
              <p className="muted">Cargando ocupacion...</p>
            )}
          </article>
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
          <MetricCard label="Clases asignadas" value={assignedClasses.length} detail="Semana actual" />
          <MetricCard label="Alumnos inscriptos" value={professorReservations.length} detail="Confirmados y espera" />
          <MetricCard label="Check-in" value="Simulado" detail="Pantalla preparada para QR" />
          <MetricCard label="Ocupacion gimnasio" value={occupancy ? `${occupancy.total.porcentaje}%` : "..."} detail="Actualiza cada 10 segundos" />
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

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Inicio administrador"
        title="Resumen operativo simulado"
        description="Metricas basicas del MVP con datos mockeados en frontend."
      />
      <section className="metric-grid">
        <MetricCard label="Reservas del dia" value={confirmedToday} detail="Confirmadas simuladas" />
        <MetricCard label="Ocupacion promedio" value={occupancy ? `${occupancy.total.porcentaje}%` : "..."} detail={occupancy?.total.estado} />
        <MetricCard label="Usuarios activos" value={activeUsers} detail={`${expiredUsers} membresias vencidas`} />
        <MetricCard label="Clase mas ocupada" value={topClass?.nombre || "-"} detail={topClass ? `${topClass.cuposOcupados}/${topClass.cupoTotal} cupos` : ""} />
      </section>
      <section className="panel">
        <h3>Estado de membresias</h3>
        <div className="actions-row">
          <StatusPill tone="success">Activas: {activeUsers}</StatusPill>
          <StatusPill tone="danger">Vencidas: {expiredUsers}</StatusPill>
        </div>
      </section>
    </div>
  );
}
