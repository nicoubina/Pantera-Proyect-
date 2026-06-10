"use client";

import { useState } from "react";
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

function getStripClass(availability) {
  if (availability.estado === "Completa") {
    return "strip-red";
  }

  if (availability.cuposDisponibles <= 3) {
    return "strip-yellow";
  }

  return "strip-green";
}

function getTodayKey() {
  return new Date().toLocaleDateString("en-CA");
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

  const todayKey = getTodayKey();
  const dates = Object.keys(groupedClasses);
  const nearestDate = dates.find((date) => date >= todayKey) || dates[0];
  const [openDay, setOpenDay] = useState(nearestDate);
  const [loadingId, setLoadingId] = useState(null);

  if (!classes.length) {
    return <EmptyState title="Sin clases" description="No hay clases cargadas para esta semana." />;
  }

  function handleAction(classId, action) {
    setLoadingId(classId);
    window.setTimeout(() => {
      action(classId);
      setLoadingId(null);
    }, 500);
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
        {Object.entries(groupedClasses).map(([date, dayClasses]) => {
          const isToday = date === todayKey;

          return (
            <article className={`day-column ${openDay === date ? "day-open" : ""}`} key={date}>
              <button
                className="day-header day-header-btn"
                type="button"
                onClick={() => setOpenDay((current) => (current === date ? null : date))}
                style={
                  isToday
                    ? { borderColor: "var(--color-orange)", background: "rgba(255,122,26,0.05)" }
                    : undefined
                }
              >
                <span style={{ display: "grid", gap: 4, textAlign: "left" }}>
                  <h3>{dayClasses[0].diaNombre}</h3>
                  <span>{date}</span>
                </span>
                <span className="material-symbols-outlined rotate-icon">expand_more</span>
              </button>
              <div className="day-cards collapsible-content">
                {dayClasses.map((classItem) => {
                  const availability = getClassAvailability(classItem);
                  const ownReservation = userReservations.find(
                    (reservation) => reservation.classId === classItem.id
                  );
                  const isConfirmed = ownReservation?.estado === "CONFIRMADA";
                  const isLoading = loadingId === classItem.id;

                  return (
                    <article
                      className={`class-card ${getStripClass(availability)} ${isConfirmed ? "class-card-confirmed" : ""}`}
                      key={classItem.id}
                    >
                      <div className="card-title-row">
                        <div>
                          <h4>{classItem.nombre}</h4>
                          <p className="muted">{classItem.profesor}</p>
                        </div>
                        {isConfirmed ? (
                          <span className="material-symbols-outlined" style={{ color: "var(--color-green)" }}>
                            check_circle
                          </span>
                        ) : (
                          <StatusPill tone={getTone(availability.estado)}>
                            {availability.estado}
                          </StatusPill>
                        )}
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

                      <div className="progress-track">
                        <span style={{ width: `${(classItem.cuposOcupados / classItem.cupoTotal) * 100}%` }} />
                      </div>

                      {ownReservation ? (
                        <StatusPill tone={ownReservation.estado === "CONFIRMADA" ? "success" : "warning"}>
                          Ya tenes estado {ownReservation.estado}
                        </StatusPill>
                      ) : availability.estado === "Completa" ? (
                        <button
                          className="secondary-button"
                          type="button"
                          disabled={isLoading}
                          onClick={() => handleAction(classItem.id, joinWaitList)}
                        >
                          {isLoading ? "Reservando..." : "Unirme a lista de espera"}
                        </button>
                      ) : (
                        <button
                          className="primary-button"
                          type="button"
                          disabled={isLoading}
                          onClick={() => handleAction(classItem.id, reserveClass)}
                        >
                          {isLoading ? "Reservando..." : "Reservar"}
                        </button>
                      )}
                    </article>
                  );
                })}
              </div>
            </article>
          );
        })}
      </section>
    </div>
  );
}
