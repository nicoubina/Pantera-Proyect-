"use client";
import { useState } from "react";
import PageHeader from "@/components/common/PageHeader";
import useModule from "./useModule";
import {
  ModuleFeedback,
  ModuleEmpty,
  Editor,
  Field,
  fullName,
  scheduleLabel,
} from "./ModuleUI";
import { modulesService } from "@/services/modulesService";
import AdminClasses from "@/components/clases/AdminClasses";

export default function ScheduleManagement({ schedules = false }) {
  const resource = schedules ? "horarios" : "clases";
  const module = useModule(resource);
  const catalog = useModule(schedules ? "clases" : "usuarios");
  const [form, setForm] = useState(null);
  const [confirm, setConfirm] = useState(null);
  const set = (key, value) => setForm((f) => ({ ...f, [key]: value }));
  function edit(r) {
    setForm(
      schedules
        ? { ...r, claseGimnasioId: r.claseGimnasio.id }
        : { ...r, profesorId: r.profesor.id },
    );
  }
  async function save(e) {
    e.preventDefault();
    const body = { ...form, cupoMaximo: Number(form.cupoMaximo) };
    if (schedules) {
      body.claseGimnasioId = Number(form.claseGimnasioId);
      delete body.diaSemana;
    } else body.profesorId = Number(form.profesorId);
    if (await module.act(() => modulesService.save(resource, form.id, body)))
      setForm(null);
  }
  return (
    <div className="stack">
      <PageHeader
        title={schedules ? "Gestión de horarios" : "Gestión de clases"}
        description={
          schedules
            ? "Horarios por fecha, cupo y clase. El profesor se toma de la clase asociada."
            : "Clases y profesores asignados."
        }
      />
      <ModuleFeedback module={module} />
      <button
        className="primary-button"
        onClick={() =>
          setForm(
            schedules
              ? {
                  claseGimnasioId: "",
                  fecha: "",
                  horaInicio: "",
                  horaFin: "",
                  cupoMaximo: 20,
                  activa: true,
                }
              : {
                  nombre: "",
                  descripcion: "",
                  profesorId: "",
                  sector: "SALA_CLASES",
                  cupoMaximo: 20,
                  activa: true,
                },
          )
        }
      >
        Crear {schedules ? "horario" : "clase"}
      </button>
      {form && (
        <Editor
          title={form.id ? "Editar" : "Crear"}
          onSubmit={save}
          onClose={() => setForm(null)}
          pending={module.pending}
        >
          <ModuleFeedback module={catalog} />
          {schedules ? (
            <>
              <Field label="Clase">
                <select
                  required
                  value={form.claseGimnasioId}
                  onChange={(e) => set("claseGimnasioId", e.target.value)}
                >
                  <option value="">Seleccionar</option>
                  {catalog.data.map((c) => (
                    <option value={c.id} key={c.id}>
                      {c.nombre} · {fullName(c.profesor)}
                    </option>
                  ))}
                </select>
              </Field>
              {[
                ["fecha", "Fecha", "date"],
                ["horaInicio", "Inicio", "time"],
                ["horaFin", "Fin", "time"],
              ].map(([key, label, type]) => (
                <Field key={key} label={label}>
                  <input
                    type={type}
                    required
                    value={form[key]}
                    onChange={(e) => set(key, e.target.value)}
                  />
                </Field>
              ))}
            </>
          ) : (
            <>
              <Field label="Nombre">
                <input
                  maxLength={255}
                  required
                  value={form.nombre}
                  onChange={(e) => set("nombre", e.target.value)}
                />
              </Field>
              <Field label="Descripción">
                <textarea
                  maxLength={1000}
                  value={form.descripcion || ""}
                  onChange={(e) => set("descripcion", e.target.value)}
                />
              </Field>
              <Field label="Profesor">
                <select
                  required
                  value={form.profesorId}
                  onChange={(e) => set("profesorId", e.target.value)}
                >
                  <option value="">Seleccionar</option>
                  {catalog.data
                    .filter((u) => u.rol === "PROFESOR" && u.activo)
                    .map((u) => (
                      <option value={u.id} key={u.id}>
                        {fullName(u)}
                      </option>
                    ))}
                </select>
              </Field>
              <Field label="Sector">
                <select
                  value={form.sector}
                  onChange={(e) => set("sector", e.target.value)}
                >
                  {["SALA_CLASES", "MUSCULACION"].map((s) => (
                    <option key={s}>{s}</option>
                  ))}
                </select>
              </Field>
            </>
          )}
          <Field label="Cupo máximo">
            <input
              type="number"
              min="1"
              required
              value={form.cupoMaximo}
              onChange={(e) => set("cupoMaximo", e.target.value)}
            />
          </Field>
          <Field label="Estado">
            <select
              value={String(form.activa)}
              onChange={(e) => set("activa", e.target.value === "true")}
            >
              <option value="true">Activo</option>
              <option value="false">Inactivo</option>
            </select>
          </Field>
        </Editor>
      )}
      <ModuleEmpty module={module} />
      {module.data.map((r) => (
        <article className="panel" key={r.id}>
          <h3>{schedules ? scheduleLabel(r) : r.nombre}</h3>
          <p className="muted">
            Profesor:{" "}
            {fullName(schedules ? r.claseGimnasio.profesor : r.profesor)} ·
            Cupo: {r.cupoMaximo} · {r.activa ? "Activo" : "Inactivo"}
          </p>
          <p>{schedules ? `Fin: ${r.horaFin.slice(0, 5)}` : r.descripcion}</p>
          <div className="actions-row">
            <button className="secondary-button" onClick={() => edit(r)}>
              Editar
            </button>
            {r.activa && (
              <button className="ghost-button" onClick={() => setConfirm(r.id)}>
                Desactivar
              </button>
            )}
          </div>
          {confirm === r.id && (
            <div className="actions-row">
              <p>¿Desactivar este registro?</p>
              <button
                className="primary-button"
                disabled={module.pending}
                onClick={async () => {
                  if (
                    await module.act(
                      () => modulesService.remove(resource, r.id),
                      "Registro desactivado.",
                    )
                  )
                    setConfirm(null);
                }}
              >
                Confirmar
              </button>
              <button className="ghost-button" onClick={() => setConfirm(null)}>
                Mantener
              </button>
            </div>
          )}
        </article>
      ))}
      {!schedules && <AdminClasses />}
    </div>
  );
}
