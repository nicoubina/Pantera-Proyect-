"use client";

import EmptyState from "@/components/common/EmptyState";
import PageHeader from "@/components/common/PageHeader";
import StatusPill from "@/components/common/StatusPill";
import { RESERVA_ESTADOS } from "@/services/reservationService";
import { useAppData } from "@/context/AppDataContext";
import { useAuth } from "@/context/AuthContext";

export default function ProfessorReservations() {
  const { user } = useAuth();
  const { classes, reservations } = useAppData();
  const assignedClassIds = classes
    .filter((classItem) => classItem.profesorId === user.id)
    .map((classItem) => classItem.id);
  const visibleReservations = reservations.filter(
    (reservation) =>
      assignedClassIds.includes(reservation.classId) &&
      reservation.estado !== RESERVA_ESTADOS.CANCELADA
  );

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Reservas"
        title="Alumnos por clase"
        description="Listado simulado de reservas, espera y asistencia."
      />

      {visibleReservations.length ? (
        <section className="reservation-list">
          {visibleReservations.map((reservation) => {
            const classItem = classes.find((item) => item.id === reservation.classId);
            return (
              <article className="reservation-card" key={reservation.id}>
                <div>
                  <h3>{reservation.userName}</h3>
                  <p className="muted">
                    {classItem?.nombre} - {classItem?.diaNombre} {classItem?.hora}
                  </p>
                </div>
                <StatusPill tone={reservation.estado === "EN_ESPERA" ? "warning" : "success"}>
                  {reservation.estado}
                </StatusPill>
              </article>
            );
          })}
        </section>
      ) : (
        <EmptyState title="Sin reservas" description="Todavia no hay alumnos inscriptos." />
      )}
    </div>
  );
}
