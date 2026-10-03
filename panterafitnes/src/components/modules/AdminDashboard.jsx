"use client";
import Link from "next/link";
import PageHeader from "@/components/common/PageHeader";
import MetricCard from "@/components/common/MetricCard";
import useModule from "./useModule";
import { ModuleFeedback, fullName } from "./ModuleUI";

export default function AdminDashboard() {
  const module = useModule("estadisticas");
  const d = module.data;
  return (
    <div className="stack">
      <PageHeader
        title="Resumen operativo"
        description="Métricas calculadas por el servidor sobre registros reales. Popularidad: reservas confirmadas, asistidas y ausentes, excluyendo cancelaciones y espera."
      />
      <ModuleFeedback module={module} />
      <button className="secondary-button" onClick={module.load}>
        Actualizar métricas
      </button>
      {!module.loading && d.ocupacion && (
        <>
          <section className="metric-grid">
            {[
              ["Usuarios", d.usuarios],
              ["Membresías activas", d.membresiasActivas],
              ["Membresías vencidas", d.membresiasVencidas],
              ["Clases", d.clases],
              ["Reservas totales", d.reservas],
              ["Asistencias", d.asistencias],
              ["Ausencias", d.ausencias],
              ["Lista de espera activa", d.listaEspera],
              ["Ocupación actual", `${d.ocupacion.porcentajeOcupacion}%`],
            ].map(([label, value]) => (
              <MetricCard key={label} label={label} value={value} />
            ))}
          </section>
          <section className="panel">
            <h3>Clases más populares</h3>
            {d.clasesPopulares.map((c) => (
              <p key={c.claseId}>
                {c.nombre}: {c.reservas} reservas
              </p>
            ))}
          </section>
          <section className="panel">
            <h3>Horarios más utilizados</h3>
            {d.horariosUtilizados.map((h) => (
              <p key={h.horarioId}>
                {h.clase} · {h.fecha} {h.hora.slice(0, 5)}: {h.reservas}{" "}
                reservas
              </p>
            ))}
            {!d.horariosUtilizados.length && <p>Sin reservas registradas.</p>}
          </section>
          <section className="panel">
            <h3>Profesores y clases asignadas</h3>
            {d.profesores.map((p) => (
              <p key={p.profesor.id}>
                {fullName(p.profesor)}: {p.clasesAsignadas} clases activas
              </p>
            ))}
          </section>
        </>
      )}
      <div className="actions-row">
        <Link className="primary-button link-button" href="/admin/usuarios">
          Usuarios
        </Link>
        <Link className="secondary-button link-button" href="/admin/horarios">
          Horarios
        </Link>
        <Link className="secondary-button link-button" href="/admin/alertas">
          Alertas
        </Link>
      </div>
    </div>
  );
}
