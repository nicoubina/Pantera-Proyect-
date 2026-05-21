"use client";

import EmptyState from "@/components/common/EmptyState";
import PageHeader from "@/components/common/PageHeader";
import StatusPill from "@/components/common/StatusPill";
import { MEMBRESIAS } from "@/data/mockUsers";
import { getClassAvailability } from "@/services/classService";
import { RESERVA_ESTADOS } from "@/services/reservationService";
import { useAppData } from "@/context/AppDataContext";
import { useAuth } from "@/context/AuthContext";

function groupByDate(classes) {
  return classes.reduce((groups, classItem) => {
    const key = classItem.fecha;
    return {
      ...groups,
      [key]: [...(groups[key] || []), classItem]
    };
  }, {});
}

function getTone(value) {
  if (value.includes("Completa") || value.includes("Alta")) {
    return "danger";
  }

  if (value.includes("Ultimos") || value.includes("Media")) {
    return "warning";
  }

  return "success";
}

export default function WeeklyClasses() {
  const { user } = useAuth();
  const { classes, reservations, reserveClass, joinWaitList } = useAppData();
  const groupedClasses = groupByDate(classes);
  const userReservations = reservations.filter(
    (reservation) =>
      reservation.userId === user.id &&
      [RESERVA_ESTADOS.CONFIRMADA, RESERVA_ESTADOS.EN_ESPERA].includes(reservation.estado)
  );

  if (!classes.length) {
    return <EmptyState title="Sin clases" description="No hay clases cargadas para esta semana." />;
  }

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Clases"
        title="Reserva semanal"
        description="Vista semanal con Funcional y Musculacion. Las validaciones son frontend y mockeadas."
      />

      {user.membresia === MEMBRESIAS.VENCIDA ? (
        <section className="warning-panel">
          Membresia vencida. Podes ver las clases, pero no confirmar reservas ni lista de espera.
        </section>
      ) : null}

      <section className="week-grid">
        {Object.entries(groupedClasses).map(([date, dayClasses]) => (
          <article className="day-column" key={date}>
            <div className="day-header">
              <h3>{dayClasses[0].diaNombre}</h3>
              <span>{date}</span>
            </div>
            <div className="day-cards">
              {dayClasses.map((classItem) => {
                const availability = getClassAvailability(classItem);
                const ownReservation = userReservations.find(
                  (reservation) => reservation.classId === classItem.id
                );

                return (
                  <article className="class-card" key={classItem.id}>
                    <div className="card-title-row">
                      <div>
                        <h4>{classItem.nombre}</h4>
                        <p className="muted">{classItem.profesor}</p>
                      </div>
                      <StatusPill tone={getTone(availability.estado)}>
                        {availability.estado}
                      </StatusPill>
                    </div>

                    <dl className="detail-list">
                      <div>
                        <dt>Hora</dt>
                        <dd>{classItem.hora}</dd>
                      </div>
                      <div>
                        <dt>Duracion</dt>
                        <dd>{classItem.duracionMinutos} min</dd>
                      </div>
                      <div>
                        <dt>Cupos</dt>
                        <dd>
                          {classItem.cuposOcupados}/{classItem.cupoTotal} ocupados
                        </dd>
                      </div>
                      <div>
                        <dt>Nivel</dt>
                        <dd>
                          {availability.nivel} - {availability.cuposDisponibles} disponibles
                        </dd>
                      </div>
                    </dl>

                    {ownReservation ? (
                      <StatusPill tone={ownReservation.estado === "CONFIRMADA" ? "success" : "warning"}>
                        Ya tenes estado {ownReservation.estado}
                      </StatusPill>
                    ) : availability.estado === "Completa" ? (
                      <button
                        className="secondary-button"
                        type="button"
                        onClick={() => joinWaitList(classItem.id)}
                      >
                        Unirme a lista de espera
                      </button>
                    ) : (
                      <button
                        className="primary-button"
                        type="button"
                        onClick={() => reserveClass(classItem.id)}
                      >
                        Reservar
                      </button>
                    )}
                  </article>
                );
              })}
            </div>
          </article>
        ))}
      </section>
    </div>
  );
}
