"use client";

import { useState } from "react";
import EmptyState from "@/components/common/EmptyState";
import MetricCard from "@/components/common/MetricCard";
import PageHeader from "@/components/common/PageHeader";
import StatusPill from "@/components/common/StatusPill";
import { getClassAvailability } from "@/services/classService";
import { RESERVA_ESTADOS } from "@/services/reservationService";
import { useAppData } from "@/context/AppDataContext";
import { useAuth } from "@/context/AuthContext";

function getStripClass(availability) {
  if (availability.estado === "Completa") {
    return "strip-red";
  }

  if (availability.cuposDisponibles <= 3) {
    return "strip-yellow";
  }

  return "strip-green";
}

function studentDotClass(estado) {
  if (estado === "CONFIRMADA" || estado === RESERVA_ESTADOS.ASISTIDA) {
    return "green";
  }

  if (estado === RESERVA_ESTADOS.EN_ESPERA) {
    return "yellow";
  }

  return "red";
}

export default function ProfessorClasses() {
  const { user } = useAuth();
  const { classes, reservations } = useAppData();
  const assignedClasses = classes.filter((classItem) => classItem.profesorId === user.id);
  const [isOpen, setIsOpen] = useState({});

  const todayKey = new Date().toLocaleDateString("en-CA");
  const classesToday = assignedClasses.filter((classItem) => classItem.fecha === todayKey).length;
  const assignedIds = assignedClasses.map((classItem) => classItem.id);
  const totalStudents = reservations.filter(
    (reservation) =>
      assignedIds.includes(reservation.classId) && reservation.estado !== RESERVA_ESTADOS.CANCELADA
  ).length;

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Profesor"
        title="Mis clases asignadas"
        description="Ocupacion, alumnos inscriptos y asistencia simulada."
      />

      <section className="split-grid">
        <MetricCard icon="calendar_month" label="Clases hoy" value={classesToday} detail="Asignadas para la fecha actual" />
        <MetricCard icon="groups" label="Alumnos totales" value={totalStudents} detail="Confirmados y en espera" />
      </section>

      {assignedClasses.length ? (
        <section className="card-grid">
          {assignedClasses.map((classItem) => {
            const availability = getClassAvailability(classItem);
            const classReservations = reservations.filter(
              (reservation) =>
                reservation.classId === classItem.id &&
                reservation.estado !== RESERVA_ESTADOS.CANCELADA
            );
            const open = Boolean(isOpen[classItem.id]);

            return (
              <article className={`class-card wide ${getStripClass(availability)}`} key={classItem.id}>
                <div className="card-title-row">
                  <div>
                    <h3>{classItem.nombre}</h3>
                    <p className="muted">
                      {classItem.diaNombre} {classItem.fecha} - {classItem.hora}
                    </p>
                  </div>
                  <StatusPill tone={availability.estado === "Completa" ? "danger" : "success"}>
                    {availability.estado}
                  </StatusPill>
                </div>
                <p>
                  {availability.nivel} - {classItem.cuposOcupados}/{classItem.cupoTotal} cupos
                  ocupados
                </p>
                <div className={open ? "collapsible-active" : ""}>
                  <button
                    type="button"
                    className="ghost-button small"
                    style={{ display: "flex", alignItems: "center", justifyContent: "space-between", width: "100%" }}
                    onClick={() =>
                      setIsOpen((current) => ({ ...current, [classItem.id]: !current[classItem.id] }))
                    }
                  >
                    <span>Ver alumnos ({classReservations.length})</span>
                    <span className="material-symbols-outlined rotate-icon">expand_more</span>
                  </button>
                  <div className="collapsible-content">
                    {classReservations.length ? (
                      classReservations.map((reservation) => (
                        <div
                          key={reservation.id}
                          style={{
                            display: "flex",
                            alignItems: "center",
                            gap: 8,
                            padding: "8px 0",
                            borderTop: "1px solid var(--color-border)"
                          }}
                        >
                          <span className={`status-dot ${studentDotClass(reservation.estado)}`} />
                          <span>{reservation.userName}</span>
                          <span className="muted" style={{ marginLeft: "auto", fontSize: "0.78rem" }}>
                            {reservation.estado}
                          </span>
                        </div>
                      ))
                    ) : (
                      <p className="muted" style={{ paddingTop: 8 }}>
                        Sin alumnos inscriptos.
                      </p>
                    )}
                  </div>
                </div>
              </article>
            );
          })}
        </section>
      ) : (
        <EmptyState title="Sin clases asignadas" description="No hay clases para este profesor." />
      )}
    </div>
  );
}
