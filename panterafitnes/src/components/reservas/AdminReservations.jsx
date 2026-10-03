"use client";

import EmptyState from "@/components/common/EmptyState";
import MetricCard from "@/components/common/MetricCard";
import PageHeader from "@/components/common/PageHeader";
import StatusPill from "@/components/common/StatusPill";
import { RESERVA_ESTADOS } from "@/services/reservationService";
import { useAppData } from "@/context/AppDataContext";
import { useState } from "react";

export default function AdminReservations() {
  const { classes, reservations, cancelReservation, pending } = useAppData();
  const [confirm, setConfirm] = useState(null);
  const waitList = reservations.filter((reservation) => reservation.estado === RESERVA_ESTADOS.EN_ESPERA);
  const confirmed = reservations.filter((reservation) => reservation.estado === RESERVA_ESTADOS.CONFIRMADA);

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Administrador"
        title="Reservas realizadas"
        description="Reservas, cancelaciones y lista de espera registradas en el gimnasio."
      />

      <section className="metric-grid">
        <MetricCard label="Confirmadas" value={confirmed.length} detail="Reservas activas" />
        <MetricCard label="Lista de espera" value={waitList.length} detail="Usuarios esperando cupo" tone="warning" />
        <MetricCard label="Canceladas" value={reservations.filter((item) => item.estado === RESERVA_ESTADOS.CANCELADA).length} detail="Historial de reservas" />
      </section>

      {reservations.length ? (
        <section className="reservation-list">
          {reservations.map((reservation) => {
            const classItem = classes.find((item) => item.id === reservation.classId) || reservation.classItem;
            return (
              <article className="reservation-card" key={reservation.id}>
                <div>
                  <h3>{reservation.userName}</h3>
                  <p className="muted">
                    {classItem?.nombre || "Clase"} - {classItem?.diaNombre} {classItem?.hora}
                  </p>
                </div>
                <StatusPill tone={reservation.estado === RESERVA_ESTADOS.EN_ESPERA ? "warning" : "neutral"}>
                  {reservation.estado}
                </StatusPill>
                {["CONFIRMADA", "EN_ESPERA"].includes(reservation.estado) && (confirm === reservation.id
                  ? <div className="actions-row"><button className="secondary-button" disabled={pending} onClick={async () => { if (await cancelReservation(reservation.id)) setConfirm(null); }}>Sí, cancelar</button><button className="ghost-button" onClick={() => setConfirm(null)}>Mantener</button></div>
                  : <button className="ghost-button" onClick={() => setConfirm(reservation.id)}>Cancelar reserva</button>)}
              </article>
            );
          })}
        </section>
      ) : (
        <EmptyState title="Sin reservas" description="No hay reservas registradas." />
      )}
    </div>
  );
}
