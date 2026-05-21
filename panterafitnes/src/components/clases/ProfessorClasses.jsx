"use client";

import EmptyState from "@/components/common/EmptyState";
import PageHeader from "@/components/common/PageHeader";
import StatusPill from "@/components/common/StatusPill";
import { getClassAvailability } from "@/services/classService";
import { RESERVA_ESTADOS } from "@/services/reservationService";
import { useAppData } from "@/context/AppDataContext";
import { useAuth } from "@/context/AuthContext";

export default function ProfessorClasses() {
  const { user } = useAuth();
  const { classes, reservations } = useAppData();
  const assignedClasses = classes.filter((classItem) => classItem.profesorId === user.id);

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Profesor"
        title="Mis clases asignadas"
        description="Ocupacion, alumnos inscriptos y asistencia simulada."
      />

      {assignedClasses.length ? (
        <section className="card-grid">
          {assignedClasses.map((classItem) => {
            const availability = getClassAvailability(classItem);
            const classReservations = reservations.filter(
              (reservation) =>
                reservation.classId === classItem.id &&
                reservation.estado !== RESERVA_ESTADOS.CANCELADA
            );

            return (
              <article className="class-card wide" key={classItem.id}>
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
                <div className="student-list">
                  <strong>Alumnos simulados</strong>
                  {classReservations.length ? (
                    classReservations.map((reservation) => (
                      <span key={reservation.id}>
                        {reservation.userName} - {reservation.estado}
                      </span>
                    ))
                  ) : (
                    <span>Sin alumnos inscriptos.</span>
                  )}
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
