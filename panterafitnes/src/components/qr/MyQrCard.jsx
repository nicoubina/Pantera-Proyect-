"use client";

import { useEffect, useMemo, useState } from "react";
import QRCode from "qrcode";
import StatusPill from "@/components/common/StatusPill";
import { RESERVA_ESTADOS } from "@/services/reservationService";
import { useAppData } from "@/context/AppDataContext";
import { useAuth } from "@/context/AuthContext";

const REFRESH_MS = 8000;

function startOf(reservation) {
  const item = reservation.classItem;
  return item ? new Date(`${item.fecha}T${item.hora}:00`) : null;
}

/**
 * Credencial del socio: QR real con su código personal. En la entrada del gimnasio recepción
 * o el profesor lo escanean y el sistema registra la asistencia con la hora del servidor.
 */
export default function MyQrCard({ compact = false }) {
  const { user } = useAuth();
  const { reservations, refresh } = useAppData();
  const [image, setImage] = useState("");

  useEffect(() => {
    let active = true;
    if (!user?.codigoQr) return undefined;
    QRCode.toDataURL(user.codigoQr, { width: 480, margin: 2, errorCorrectionLevel: "M" })
      .then((url) => { if (active) setImage(url); })
      .catch(() => { if (active) setImage(""); });
    return () => { active = false; };
  }, [user?.codigoQr]);

  // Mientras el QR está en pantalla se actualiza solo: así el socio ve su ingreso al instante.
  useEffect(() => {
    if (compact) return undefined;
    const timer = setInterval(() => {
      if (document.visibilityState === "visible") refresh();
    }, REFRESH_MS);
    return () => clearInterval(timer);
  }, [compact, refresh]);

  const today = useMemo(() => {
    const now = new Date();
    const sameDay = (d) => d && d.toDateString() === now.toDateString();
    return reservations
      .filter((r) => r.userId === user.id && r.classItem)
      .filter((r) => sameDay(startOf(r)) && [RESERVA_ESTADOS.CONFIRMADA, RESERVA_ESTADOS.ASISTIDA, RESERVA_ESTADOS.AUSENTE].includes(r.estado))
      .sort((a, b) => startOf(a) - startOf(b));
  }, [reservations, user.id]);

  const active = user.membresia === "ACTIVA";

  return (
    <section className="panel qr-panel" data-testid="mi-qr">
      <div className="qr-image-box">
        {image ? (
          <img className="qr-image" src={image} alt={`Código QR de ingreso de ${user.nombre}`} />
        ) : (
          <div className="qr-image placeholder" aria-hidden="true" />
        )}
      </div>

      <div className="qr-content">
        <div className="access-card">
          <p className="eyebrow">Credencial de ingreso</p>
          <h3>{user.nombre}</h3>
          <p className="muted qr-code-text">{user.codigoQr}</p>
          <StatusPill tone={active ? "success" : "danger"}>Membresía {user.membresia}</StatusPill>
        </div>

        <p className="muted">
          Mostrá este QR en recepción o al profesor al llegar. Se registra desde 30 minutos antes
          de la clase; si llegás más de 10 minutos tarde, la asistencia queda como ausente.
        </p>
        {!active && (
          <p className="inline-warning">
            Con la membresía {user.membresia?.toLowerCase()} no se registra el ingreso. Renovala en recepción.
          </p>
        )}

        {!compact && (
          <div className="stack" style={{ gap: 8 }}>
            <p className="eyebrow">Tus clases de hoy</p>
            {today.length ? today.map((r) => (
              <div className="reservation-select-card" key={r.id}>
                <span style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  <span className="material-symbols-outlined" style={{ fontSize: 20 }}>fitness_center</span>
                  {r.classItem.nombre} · {r.classItem.hora} a {r.classItem.horaFin}
                </span>
                <StatusPill tone={r.estado === RESERVA_ESTADOS.ASISTIDA ? "success" : r.estado === RESERVA_ESTADOS.AUSENTE ? "danger" : "warning"}>
                  {r.estado === RESERVA_ESTADOS.ASISTIDA ? "Ingreso registrado" : r.estado === RESERVA_ESTADOS.AUSENTE ? "Ausente" : "Pendiente de ingreso"}
                </StatusPill>
              </div>
            )) : (
              <p className="muted">Hoy no tenés clases reservadas.</p>
            )}
          </div>
        )}
      </div>
    </section>
  );
}
