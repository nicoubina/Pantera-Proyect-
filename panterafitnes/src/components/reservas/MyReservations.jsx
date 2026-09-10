"use client";

import { useState } from "react";
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

function statusStrip(status) {
  const strips = {
    [RESERVA_ESTADOS.CONFIRMADA]: "strip-green",
    [RESERVA_ESTADOS.EN_ESPERA]: "strip-yellow",
    [RESERVA_ESTADOS.AUSENTE]: "strip-red"
  };

  return strips[status] || "strip-neutral";
}

const TABS = ["TODAS", "CONFIRMADAS", "EN_ESPERA", "HISTORIAL"];
const HISTORY_STATES = [RESERVA_ESTADOS.ASISTIDA, RESERVA_ESTADOS.AUSENTE, RESERVA_ESTADOS.CANCELADA];

function tabLabel(tab) {
  if (tab === "EN_ESPERA") {
    return "En Espera";
  }

  return tab.charAt(0) + tab.slice(1).toLowerCase();
}

function matchesTab(tab, estado) {
  if (tab === "TODAS") {
    return true;
  }

  if (tab === "CONFIRMADAS") {
    return estado === RESERVA_ESTADOS.CONFIRMADA;
  }

  if (tab === "EN_ESPERA") {
    return estado === RESERVA_ESTADOS.EN_ESPERA;
  }

  return HISTORY_STATES.includes(estado);
}

export default function MyReservations() {
  const { user } = useAuth();
  const { classes, reservations, cancelReservation, pending } = useAppData();
  const myReservations = reservations.filter((reservation) => reservation.userId === user.id);
  const [activeTab, setActiveTab] = useState("TODAS");
  const [isConfirming, setIsConfirming] = useState({});

  const filteredReservations = myReservations.filter((reservation) =>
    matchesTab(activeTab, reservation.estado)
  );

  function getWaitlistPosition(reservation) {
    return reservation.posicionListaEspera ?? "—";
  }

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Mis reservas"
        title="Reservas y lista de espera"
        description="Consultá tus reservas y solicitá cancelaciones. El gimnasio valida los plazos."
      />

      <div className="no-scrollbar" style={{ display: "flex", gap: 8, overflowX: "auto" }}>
        {TABS.map((tab) => (
          <button
            key={tab}
            type="button"
            className={`tab-pill ${activeTab === tab ? "active" : ""}`}
            onClick={() => setActiveTab(tab)}
          >
            {tabLabel(tab)}
          </button>
        ))}
      </div>

      {filteredReservations.length ? (
        <section className="reservation-list">
          {filteredReservations.map((reservation) => {
            const classItem = classes.find((item) => item.id === reservation.classId) || reservation.classItem;
            const canCancel = isReservationCancelable(reservation, classItem);
            const isHistory = HISTORY_STATES.includes(reservation.estado);

            return (
              <article
                className={`reservation-card ${statusStrip(reservation.estado)}`}
                key={reservation.id}
                style={isHistory ? { opacity: 0.55 } : undefined}
              >
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
                  {reservation.estado === RESERVA_ESTADOS.EN_ESPERA ? (
                    <p className="muted" style={{ display: "flex", alignItems: "center", gap: 6 }}>
                      <span className="material-symbols-outlined" style={{ fontSize: 18 }}>
                        hourglass_empty
                      </span>
                      Posición en lista: #{getWaitlistPosition(reservation)}
                    </p>
                  ) : null}
                </div>

                {canCancel ? (
                  isConfirming[reservation.id] ? (
                    <div style={{ display: "flex", gap: 8 }}>
                      <button
                        className="ghost-button small"
                        type="button"
                        disabled={pending}
                        style={{ borderColor: "var(--color-red)", color: "var(--color-red)" }}
                        onClick={() => cancelReservation(reservation.id)}
                      >
                        Sí, cancelar
                      </button>
                      <button
                        className="ghost-button small"
                        type="button"
                        onClick={() =>
                          setIsConfirming((current) => ({ ...current, [reservation.id]: false }))
                        }
                      >
                        No, mantener
                      </button>
                    </div>
                  ) : (
                    <button
                      className="secondary-button"
                      type="button"
                      onClick={() =>
                        setIsConfirming((current) => ({ ...current, [reservation.id]: true }))
                      }
                    >
                      Cancelar
                    </button>
                  )
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
