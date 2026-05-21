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

  useEffect(() => {
    if (!reservationId && confirmedReservations.length) {
      setReservationId(confirmedReservations[0].id);
    }
  }, [confirmedReservations, reservationId]);

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
        {qrCells.map((cell, index) => (
          <span className={cell ? "filled" : ""} key={`${cell}-${index}`} />
        ))}
      </div>

      <div className="qr-content">
        <p className="eyebrow">QR Simulado</p>
        <h3>QR-PANTERA-{user.id.toUpperCase()}</h3>
        <p>{user.nombre}</p>
        <StatusPill tone={user.membresia === "ACTIVA" ? "success" : "danger"}>
          Membresia {user.membresia}
        </StatusPill>
        <p className="muted">Este QR es simulado para el MVP.</p>

        {confirmedReservations.length ? (
          <label>
            Reserva confirmada para simular ingreso
            <select value={reservationId} onChange={(event) => setReservationId(event.target.value)}>
              {confirmedReservations.map((reservation) => {
                const classItem = classes.find((item) => item.id === reservation.classId);
                return (
                  <option value={reservation.id} key={reservation.id}>
                    {classItem
                      ? `${classItem.nombre} - ${classItem.diaNombre} ${classItem.hora}`
                      : "Reserva confirmada"}
                  </option>
                );
              })}
            </select>
          </label>
        ) : (
          <p className="inline-warning">No tenes una reserva confirmada para este horario.</p>
        )}

        <div className="actions-row">
          <button
            className="primary-button"
            type="button"
            disabled={!reservationId}
            onClick={() => simulateQr(reservationId, "ASISTIDA")}
          >
            Simular ingreso
          </button>
          <button
            className="secondary-button"
            type="button"
            disabled={!reservationId}
            onClick={() => simulateQr(reservationId, "AUSENTE")}
          >
            Simular +10 min tarde
          </button>
        </div>
      </div>
    </section>
  );
}
