"use client";

import PageHeader from "@/components/common/PageHeader";
import StatusPill from "@/components/common/StatusPill";
import { getClassAvailability } from "@/services/classService";
import { useAppData } from "@/context/AppDataContext";

export default function AdminClasses() {
  const { classes } = useAppData();
  const fullClasses = classes.filter((classItem) => classItem.cuposOcupados >= classItem.cupoTotal);

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Administrador"
        title="Ocupacion por clase"
        description="Control basico de cupos completos y ocupacion simulada."
      />

      <section className="panel">
        <h3>Clases con cupo completo</h3>
        <p className="muted">
          {fullClasses.length
            ? `${fullClasses.length} clases estan completas.`
            : "No hay clases completas en este momento."}
        </p>
      </section>

      <section className="card-grid">
        {classes.map((classItem) => {
          const availability = getClassAvailability(classItem);
          return (
            <article className="class-card wide" key={classItem.id}>
              <div className="card-title-row">
                <div>
                  <h3>{classItem.nombre}</h3>
                  <p className="muted">
                    {classItem.profesor} - {classItem.diaNombre} {classItem.hora}
                  </p>
                </div>
                <StatusPill tone={availability.estado === "Completa" ? "danger" : "success"}>
                  {availability.estado}
                </StatusPill>
              </div>
              <p>
                {availability.nivel} - {classItem.cuposOcupados}/{classItem.cupoTotal} cupos
                ocupados - {availability.cuposDisponibles} disponibles
              </p>
            </article>
          );
        })}
      </section>
    </div>
  );
}
