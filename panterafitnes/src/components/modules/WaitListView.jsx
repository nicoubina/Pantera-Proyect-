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

export default function WaitListView() {
  const module = useModule("lista-espera");
  const [schedule, setSchedule] = useState("");
  const schedules = [
    ...new Map(
      module.data.map((l) => [l.horarioClase.id, l.horarioClase]),
    ).values(),
  ];
  return (
    <div className="stack">
      <PageHeader
        title="Lista de espera"
        description="La asignación de cupos se realiza automáticamente al cancelar una reserva, respetando el orden y la habilitación del cliente."
      />
      <ModuleFeedback module={module} />
      <label>
        Clase y horario
        <select value={schedule} onChange={(e) => setSchedule(e.target.value)}>
          <option value="">Todos</option>
          {schedules.map((h) => (
            <option value={h.id} key={h.id}>
              {scheduleLabel(h)}
            </option>
          ))}
        </select>
      </label>
      <ModuleEmpty module={module} />
      {module.data
        .filter((l) => !schedule || l.horarioClase.id === Number(schedule))
        .map((l) => (
          <article className="reservation-card" key={l.id}>
            <div>
              <h3>{scheduleLabel(l.horarioClase)}</h3>
              <p>{fullName(l.usuario)}</p>
              <p className="muted">
                Ingreso: {l.fechaIngreso.replace("T", " ")}
              </p>
              <p>
                {l.activa
                  ? `Posición: ${l.posicion}`
                  : "Fuera de la lista activa"}
              </p>
            </div>
            <State value={l.activa ? "EN_ESPERA" : l.estado} />
          </article>
        ))}
    </div>
  );
}
