"use client";

import EmptyState from "@/components/common/EmptyState";
import PageHeader from "@/components/common/PageHeader";
import StatusPill from "@/components/common/StatusPill";
import { isReservationCancelable, RESERVA_ESTADOS } from "@/services/reservationService";
import { useAppData } from "@/context/AppDataContext";
import { useAuth } from "@/context/AuthContext";

function statusTone(status) {
  const tones = {
    [RESERVA_ESTADOS.CONFIRMADA]: "success",
    [RESERVA_ESTADOS.CANCELADA]: "neutral",
    [RESERVA_ESTADOS.EN_ESPERA]: "warning",
    [RESERVA_ESTADOS.ASISTIDA]: "success",
    [RESERVA_ESTADOS.AUSENTE]: "danger"
  };

  return tones[status] || "neutral";
}

export default function MyReservations() {
  const { user } = useAuth();
  const { classes, reservations, cancelReservation } = useAppData();
  const myReservations = reservations.filter((reservation) => reservation.userId === user.id);

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Mis reservas"
        title="Reservas y lista de espera"
        description="Aca podes cancelar reservas habilitadas y ver estados simulados."
      />

      {myReservations.length ? (
        <section className="reservation-list">
          {myReservations.map((reservation) => {
            const classItem = classes.find((item) => item.id === reservation.classId);
            const canCancel = isReservationCancelable(reservation, classItem);

            return (
              <article className="reservation-card" key={reservation.id}>
                <div>
                  <div className="card-title-row">
                    <h3>{classItem?.nombre || "Clase no disponible"}</h3>
                    <StatusPill tone={statusTone(reservation.estado)}>
                      {reservation.estado}
                    </StatusPill>
                  </div>
                  {classItem ? (
                    <p className="muted">
                      {classItem.profesor} - {classItem.diaNombre} {classItem.fecha} a las{" "}
                      {classItem.hora}
                    </p>
                  ) : (
                    <p className="muted">La clase ya no esta en la semana actual.</p>
                  )}
                  {!canCancel && reservation.estado === RESERVA_ESTADOS.CONFIRMADA ? (
                    <p className="inline-warning">
                      No se puede cancelar por estar dentro de las 24 horas previas.
                    </p>
                  ) : null}
                </div>

                {canCancel ? (
                  <button
                    className="secondary-button"
                    type="button"
                    onClick={() => cancelReservation(reservation.id)}
                  >
                    Cancelar
                  </button>
                ) : null}
              </article>
            );
          })}
        </section>
      ) : (
        <EmptyState
          title="Todavia no tenes reservas"
          description="Entrá a Clases para reservar o sumarte a lista de espera."
        />
      )}
    </div>
  );
}
