"use client";

import MetricCard from "@/components/common/MetricCard";
import PageHeader from "@/components/common/PageHeader";
import StatusPill from "@/components/common/StatusPill";
import { getClassAvailability } from "@/services/classService";
import { useAppData } from "@/context/AppDataContext";

function getTone(status) {
  if (status.includes("Alta")) {
    return "danger";
  }

  if (status.includes("Media")) {
    return "warning";
  }

  return "success";
}

function OccupancyBlock({ item, large = false }) {
  const tone = getTone(item.estado);

  return (
    <article className={`occupancy-block ${tone} ${large ? "large" : ""}`}>
      <div>
        <h3>{item.nombre}</h3>
        <p>
          {item.estado} - {item.personas}/{item.capacidad} personas
        </p>
      </div>
      <strong>{item.porcentaje}%</strong>
      <div className="progress-track" aria-hidden="true">
        <span style={{ width: `${item.porcentaje}%` }} />
      </div>
    </article>
  );
}

export default function OccupancyDashboard() {
  const { occupancy, classes } = useAppData();

  if (!occupancy) {
    return (
      <section className="panel">
        <p className="muted">Cargando ocupacion simulada...</p>
      </section>
    );
  }

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Ocupacion"
        title="Mapa visual del gimnasio"
        description="Los valores se actualizan automaticamente cada 10 segundos con datos simulados."
      />

      <section className="metric-grid">
        <MetricCard
          label="Capacidad total"
          value={`${occupancy.total.capacidad} personas`}
          detail="Gimnasio completo"
        />
        <MetricCard
          label="Ocupacion actual"
          value={`${occupancy.total.personas} personas`}
          detail={`${occupancy.total.estado} - ${occupancy.total.porcentaje}%`}
          tone={getTone(occupancy.total.estado)}
        />
        <MetricCard
          label="Sectores activos"
          value={occupancy.sectores.length}
          detail="Musculacion y clases"
        />
      </section>

      <section className="gym-map">
        <OccupancyBlock item={occupancy.total} large />
        <div className="zone-grid">
          {occupancy.sectores.map((sector) => (
            <OccupancyBlock item={sector} key={sector.id} />
          ))}
        </div>
      </section>

      <section className="panel">
        <div className="section-title-row">
          <div>
            <h3>Ocupacion por clase</h3>
            <p className="muted">Capacidad por clase: 20 personas.</p>
          </div>
        </div>
        <div className="card-grid">
          {classes.map((classItem) => {
            const availability = getClassAvailability(classItem);
            return (
              <article className="mini-card" key={classItem.id}>
                <div className="card-title-row">
                  <h4>{classItem.nombre}</h4>
                  <StatusPill tone={getTone(availability.nivel)}>{availability.estado}</StatusPill>
                </div>
                <p>
                  {availability.nivel} - {classItem.cuposOcupados}/{classItem.cupoTotal} cupos
                  ocupados
                </p>
                <p className="muted">
                  {classItem.diaNombre} {classItem.hora} - {classItem.profesor}
                </p>
              </article>
            );
          })}
        </div>
      </section>
    </div>
  );
}
