"use client";
import { useState } from "react";
import PageHeader from "@/components/common/PageHeader";
import useModule from "./useModule";
import {
  ModuleFeedback,
  ModuleEmpty,
  State,
  fullName,
  scheduleLabel,
} from "./ModuleUI";
import { useAuth } from "@/context/AuthContext";
import { useAppData } from "@/context/AppDataContext";
import { modulesService } from "@/services/modulesService";

export default function AttendanceView() {
  const module = useModule("asistencias");
  const { user } = useAuth();
  const { reservations } = useAppData();
  const [state, setState] = useState("");
  const [search, setSearch] = useState("");
  const rows = module.data.filter(
    (a) =>
      (!state || a.estadoAsistencia === state) &&
      `${fullName(a.usuario)} ${a.usuario.email} ${scheduleLabel(a.horarioClase)}`
        .toLowerCase()
        .includes(search.toLowerCase()),
  );
  return (
    <div className="stack">
      <PageHeader
        title="Historial de asistencias"
        description="Asistencias, ausencias y cancelaciones registradas por horario."
      />
      <ModuleFeedback module={module} />
      <section className="module-grid">
        <label>
          Buscar usuario o clase
          <input value={search} onChange={(e) => setSearch(e.target.value)} />
        </label>
        <label>
          Estado
          <select value={state} onChange={(e) => setState(e.target.value)}>
            <option value="">Todos</option>
            {["PENDIENTE", "ASISTIDA", "AUSENTE", "CANCELADA"].map((s) => (
              <option key={s}>{s}</option>
            ))}
          </select>
        </label>
      </section>
      <ModuleEmpty module={module} />
      {!module.loading && module.data.length > 0 && !rows.length && (
        <p className="panel">No hay registros para estos filtros.</p>
      )}
      {rows.map((a) => (
        <article className="reservation-card" key={a.id}>
          <div>
            <h3>{scheduleLabel(a.horarioClase)}</h3>
            <p>{fullName(a.usuario)}</p>
            <p className="muted">
              Ingreso: {a.horaIngreso?.replace("T", " ") || "Sin ingreso"} ·{" "}
              {a.metodoRegistro}
            </p>
          </div>
          <State value={a.estadoAsistencia} />
        </article>
      ))}
      {user.rol !== "CLIENTE" && (
        <section className="panel">
          <h3>Registrar asistencia de alumnos</h3>
          <p className="muted">
            Disponible desde el inicio de la clase. Las clases finalizadas sin
            ingreso se cierran automáticamente.
          </p>
          {reservations
            .filter((r) => r.estado === "CONFIRMADA")
            .map((r) => (
              <article className="reservation-card" key={r.id}>
                <div>
                  <strong>{r.userName}</strong>
                  <p>
                    {r.classItem.nombre} · {r.classItem.fecha}{" "}
                    {r.classItem.hora}
                  </p>
                </div>
                <div className="actions-row">
                  {["ASISTIDA", "AUSENTE"].map((s) => (
                    <button
                      className="secondary-button"
                      key={s}
                      disabled={module.pending}
                      onClick={() =>
                        module.act(
                          () =>
                            modulesService.save("asistencias", null, {
                              reservaId: r.id,
                              estadoAsistencia: s,
                            }),
                          "Asistencia registrada.",
                        )
                      }
                    >
                      {s === "ASISTIDA" ? "Presente" : "Ausente"}
                    </button>
                  ))}
                </div>
              </article>
            ))}
        </section>
      )}
    </div>
  );
}
