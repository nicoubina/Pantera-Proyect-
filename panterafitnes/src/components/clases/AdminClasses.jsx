"use client";

import { useState } from "react";
import MetricCard from "@/components/common/MetricCard";
import PageHeader from "@/components/common/PageHeader";
import StatusPill from "@/components/common/StatusPill";
import { getClassAvailability } from "@/services/classService";
import { useAppData } from "@/context/AppDataContext";

const TABS = ["Todas", "Completas", "Alta ocupación"];

function getStripClass(availability) {
  if (availability.estado === "Completa") {
    return "strip-red";
  }

  if (availability.cuposDisponibles <= 3) {
    return "strip-yellow";
  }

  return "strip-green";
}

export default function AdminClasses() {
  const { classes } = useAppData();
  const [activeTab, setActiveTab] = useState("Todas");

  const fullClasses = classes.filter((classItem) => classItem.cuposOcupados >= classItem.cupoTotal);
  const highOccupancyClasses = classes.filter(
    (classItem) => getClassAvailability(classItem).porcentaje >= 80
  );

  const filteredClasses = classes.filter((classItem) => {
    if (activeTab === "Completas") {
      return classItem.cuposOcupados >= classItem.cupoTotal;
    }

    if (activeTab === "Alta ocupación") {
      return getClassAvailability(classItem).porcentaje >= 80;
    }

    return true;
  });

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Administrador"
        title="Ocupacion por clase"
        description="Control basico de cupos completos y ocupacion simulada."
      />

      <section className="metric-grid">
        <MetricCard icon="fitness_center" label="Clases activas" value={classes.length} detail="Semana actual" />
        <MetricCard
          icon="error"
          label="Cupo completo"
          value={fullClasses.length}
          detail={fullClasses.length ? "Requieren atención" : "Sin clases completas"}
          tone={fullClasses.length ? "danger" : "neutral"}
        />
        <MetricCard
          icon="monitoring"
          label="Alta ocupación"
          value={highOccupancyClasses.length}
          detail="Clases con 80% o más de ocupación"
          tone={highOccupancyClasses.length ? "warning" : "neutral"}
        />
      </section>

      <div className="no-scrollbar" style={{ display: "flex", gap: 8, overflowX: "auto" }}>
        {TABS.map((tab) => (
          <button
            key={tab}
            type="button"
            className={`tab-pill ${activeTab === tab ? "active" : ""}`}
            onClick={() => setActiveTab(tab)}
          >
            {tab}
          </button>
        ))}
      </div>

      <section className="card-grid">
        {filteredClasses.map((classItem) => {
          const availability = getClassAvailability(classItem);
          const isFull = availability.estado === "Completa";

          return (
            <article
              className={`class-card wide ${getStripClass(availability)}`}
              key={classItem.id}
              style={{
                position: "relative",
                ...(isFull ? { border: "2px solid var(--color-red)" } : {})
              }}
            >
              {isFull ? (
                <span
                  className="status-pill danger"
                  style={{ position: "absolute", top: 12, right: 12 }}
                >
                  COMPLETA
                </span>
              ) : null}
              <div className="card-title-row">
                <div>
                  <h3>{classItem.nombre}</h3>
                  <p className="muted">
                    {classItem.profesor} - {classItem.diaNombre} {classItem.hora}
                  </p>
                </div>
                {!isFull ? (
                  <StatusPill tone="success">{availability.estado}</StatusPill>
                ) : null}
              </div>
              <p>
                {availability.nivel} - {classItem.cuposOcupados}/{classItem.cupoTotal} cupos
                ocupados - {availability.cuposDisponibles} disponibles
              </p>
              <div className="progress-track">
                <span style={{ width: `${availability.porcentaje}%` }} />
              </div>
              <p className="muted" style={{ fontSize: "0.8rem" }}>{availability.porcentaje}% ocupado</p>
            </article>
          );
        })}
      </section>
    </div>
  );
}
