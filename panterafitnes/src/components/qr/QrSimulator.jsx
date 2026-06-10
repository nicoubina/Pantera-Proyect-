"use client";

import { useEffect, useMemo, useState } from "react";
import StatusPill from "@/components/common/StatusPill";
import { ROLES } from "@/data/mockUsers";
import { RESERVA_ESTADOS } from "@/services/reservationService";
import { useAppData } from "@/context/AppDataContext";
import { useAuth } from "@/context/AuthContext";

const qrCells = [
  1, 1, 1, 0, 1, 0, 1, 1,
  1, 0, 0, 0, 1, 1, 0, 1,
  1, 0, 1, 1, 0, 1, 0, 1,
  0, 0, 1, 0, 1, 0, 1, 0,
  1, 1, 0, 1, 1, 0, 0, 1,
  0, 1, 1, 0, 0, 1, 1, 0,
  1, 0, 0, 1, 0, 1, 0, 1,
  1, 1, 1, 0, 1, 0, 1, 1
];

export default function QrSimulator() {
  const { user } = useAuth();
  const { classes, reservations, simulateQr } = useAppData();
  const confirmedReservations = useMemo(
    () =>
      reservations.filter(
        (reservation) =>
          reservation.userId === user.id && reservation.estado === RESERVA_ESTADOS.CONFIRMADA
      ),
    [reservations, user.id]
  );
  const [reservationId, setReservationId] = useState("");
  const [qrResult, setQrResult] = useState(null);

  useEffect(() => {
    if (!reservationId && confirmedReservations.length) {
      setReservationId(confirmedReservations[0].id);
    }
  }, [confirmedReservations, reservationId]);

  useEffect(() => {
    if (!qrResult) {
      return undefined;
    }

    const timer = setTimeout(() => setQrResult(null), 5000);
    return () => clearTimeout(timer);
  }, [qrResult]);

  function handleSimulate(mode) {
    simulateQr(reservationId, mode);
    setQrResult(
      mode === "AUSENTE"
        ? { tone: "error", icon: "schedule", message: "Ingreso registrado con +10 min de tardanza." }
        : { tone: "success", icon: "check_circle", message: "Ingreso simulado correctamente." }
    );
  }

  if (user.rol !== ROLES.CLIENTE) {
    return (
      <section className="panel qr-panel">
        <div>
          <p className="eyebrow">QR / Check-in</p>
          <h3>Pantalla preparada</h3>
          <p className="muted">
            En esta etapa no hay molinetes reales ni integracion externa. La vista queda lista
            para conectar un check-in real mas adelante.
          </p>
        </div>
      </section>
    );
  }

  return (
    <section className="panel qr-panel">
      <div className="qr-visual" aria-label="QR visual simulado">
        <span className="qr-finder top-left" />
        <span className="qr-finder top-right" />
        <span className="qr-finder bottom-left" />
        {qrCells.map((cell, index) => (
          <span className={cell ? "filled" : ""} key={`${cell}-${index}`} />
        ))}
      </div>

      <div className="qr-content">
        <div className="access-card">
          <p className="eyebrow">QR Simulado</p>
          <h3 className="font-display">QR-PANTERA-{user.id.toUpperCase()}</h3>
          <p>{user.nombre}</p>
          <StatusPill tone={user.membresia === "ACTIVA" ? "success" : "danger"}>
            Membresia {user.membresia}
          </StatusPill>
        </div>
        <p className="muted">Este QR es simulado para el MVP.</p>

        {confirmedReservations.length ? (
          <div>
            <p className="muted" style={{ marginBottom: 8, fontSize: "0.78rem" }}>
              Reserva confirmada para simular ingreso
            </p>
            <div className="stack" style={{ gap: 8 }}>
              {confirmedReservations.map((reservation) => {
                const classItem = classes.find((item) => item.id === reservation.classId);
                const isSelected = reservation.id === reservationId;

                return (
                  <button
                    type="button"
                    key={reservation.id}
                    className={`reservation-select-card ${isSelected ? "selection-active" : ""}`}
                    onClick={() => setReservationId(reservation.id)}
                  >
                    <span style={{ display: "flex", alignItems: "center", gap: 8 }}>
                      <span className="material-symbols-outlined" style={{ fontSize: 20 }}>
                        fitness_center
                      </span>
                      {classItem
                        ? `${classItem.nombre} - ${classItem.diaNombre} ${classItem.hora}`
                        : "Reserva confirmada"}
                    </span>
                    {isSelected ? (
                      <span className="material-symbols-outlined" style={{ fontSize: 20, color: "var(--color-orange)" }}>
                        check_circle
                      </span>
                    ) : null}
                  </button>
                );
              })}
            </div>
          </div>
        ) : (
          <p className="inline-warning">No tenes una reserva confirmada para este horario.</p>
        )}

        <div className={`result-banner ${qrResult ? `visible ${qrResult.tone}` : ""}`}>
          {qrResult ? (
            <>
              <span className="material-symbols-outlined">{qrResult.icon}</span>
              <span>{qrResult.message}</span>
            </>
          ) : null}
        </div>

        <div className="actions-row">
          <button
            className="primary-button"
            type="button"
            disabled={!reservationId}
            onClick={() => handleSimulate("ASISTIDA")}
          >
            <span className="material-symbols-outlined" style={{ verticalAlign: "middle", marginRight: 6 }}>
              qr_code_scanner
            </span>
            Simular ingreso
          </button>
          <button
            className="secondary-button"
            type="button"
            disabled={!reservationId}
            onClick={() => handleSimulate("AUSENTE")}
          >
            <span className="material-symbols-outlined" style={{ verticalAlign: "middle", marginRight: 6 }}>
              schedule
            </span>
            Simular +10 min tarde
          </button>
        </div>
      </div>
    </section>
  );
}
