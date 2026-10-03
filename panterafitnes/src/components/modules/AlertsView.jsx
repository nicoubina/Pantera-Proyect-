"use client";
import { useState } from "react";
import PageHeader from "@/components/common/PageHeader";
import useModule from "./useModule";
import { ModuleFeedback, ModuleEmpty, Editor, Field } from "./ModuleUI";
import { modulesService } from "@/services/modulesService";

export default function AlertsView() {
  const module = useModule("alertas");
  const [form, setForm] = useState(null);
  const set = (key, value) => setForm((f) => ({ ...f, [key]: value }));
  async function save(e) {
    e.preventDefault();
    if (await module.act(() => modulesService.save("alertas", form.id, form)))
      setForm(null);
  }
  return (
    <div className="stack">
      <PageHeader
        title="Gestión de alertas"
        description="Avisos internos destacados para clientes y profesores."
      />
      <ModuleFeedback module={module} />
      <button
        className="primary-button"
        onClick={() =>
          setForm({
            titulo: "",
            descripcion: "",
            activa: true,
            prioridad: "MEDIA",
          })
        }
      >
        Crear alerta
      </button>
      {form && (
        <Editor
          title={form.id ? "Editar alerta" : "Nueva alerta"}
          onSubmit={save}
          onClose={() => setForm(null)}
          pending={module.pending}
        >
          <Field label="Título">
            <input
              required
              maxLength={255}
              value={form.titulo}
              onChange={(e) => set("titulo", e.target.value)}
            />
          </Field>
          <Field label="Descripción">
            <textarea
              required
              maxLength={1000}
              value={form.descripcion}
              onChange={(e) => set("descripcion", e.target.value)}
            />
          </Field>
          <Field label="Prioridad">
            <select
              value={form.prioridad}
              onChange={(e) => set("prioridad", e.target.value)}
            >
              {["BAJA", "MEDIA", "ALTA"].map((p) => (
                <option key={p}>{p}</option>
              ))}
            </select>
          </Field>
          <Field label="Estado">
            <select
              value={String(form.activa)}
              onChange={(e) => set("activa", e.target.value === "true")}
            >
              <option value="true">Activa</option>
              <option value="false">Inactiva</option>
            </select>
          </Field>
        </Editor>
      )}
      <ModuleEmpty module={module} />
      {module.data.map((a) => (
        <article className="panel" key={a.id}>
          <div className="section-title-row">
            <h3>{a.titulo}</h3>
            <span className="status-pill">
              {a.prioridad} · {a.activa ? "Activa" : "Inactiva"}
            </span>
          </div>
          <p>{a.descripcion}</p>
          <p className="muted">{a.fechaCreacion.replace("T", " ")}</p>
          <div className="actions-row">
            <button className="secondary-button" onClick={() => setForm(a)}>
              Editar
            </button>
            <button
              className="ghost-button"
              disabled={module.pending}
              onClick={() =>
                module.act(() =>
                  modulesService.save("alertas", a.id, {
                    ...a,
                    activa: !a.activa,
                  }),
                )
              }
            >
              {a.activa ? "Desactivar" : "Activar"}
            </button>
          </div>
        </article>
      ))}
    </div>
  );
}
