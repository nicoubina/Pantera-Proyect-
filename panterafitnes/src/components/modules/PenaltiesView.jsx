"use client";
import { useState } from "react";
import PageHeader from "@/components/common/PageHeader";
import useModule from "./useModule";
import {
  ModuleFeedback,
  ModuleEmpty,
  State,
  fullName,
  Editor,
  Field,
} from "./ModuleUI";
import { useAuth } from "@/context/AuthContext";
import { useAppData } from "@/context/AppDataContext";
import { modulesService } from "@/services/modulesService";

export default function PenaltiesView() {
  const module = useModule("penalizaciones");
  const { user } = useAuth();
  const { users } = useAppData();
  const [form, setForm] = useState(null);
  const [confirm, setConfirm] = useState(null);
  const [detail, setDetail] = useState(null);
  const admin = user.rol === "ADMINISTRADOR";
  const set = (key, value) => setForm((f) => ({ ...f, [key]: value }));
  async function save(e) {
    e.preventDefault();
    if (
      await module.act(() =>
        modulesService.save("penalizaciones", null, {
          ...form,
          usuarioId: Number(form.usuarioId),
        }),
      )
    )
      setForm(null);
  }
  return (
    <div className="stack">
      <PageHeader
        title={admin ? "Gestión de penalizaciones" : "Mis penalizaciones"}
        description="Tres faltas generan una sanción: un mes la primera vez, tres meses las siguientes. El conteo se reinicia en ciclos de tres meses desde la primera falta."
      />
      <ModuleFeedback module={module} />
      <p className="panel">
        Una penalización activa bloquea las reservas anticipadas. Podés reservar
        con cupo durante los 30 minutos previos al inicio.
      </p>
      {admin && (
        <button
          className="primary-button"
          onClick={() =>
            setForm({
              usuarioId: "",
              motivo: "",
              fechaInicio: "",
              fechaFin: "",
            })
          }
        >
          Registrar penalización
        </button>
      )}
      {form && (
        <Editor
          title="Nueva penalización"
          onSubmit={save}
          onClose={() => setForm(null)}
          pending={module.pending}
        >
          <Field label="Cliente">
            <select
              required
              value={form.usuarioId}
              onChange={(e) => set("usuarioId", e.target.value)}
            >
              <option value="">Seleccionar</option>
              {users
                .filter((u) => u.rol === "CLIENTE")
                .map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.nombre} · {u.email}
                  </option>
                ))}
            </select>
          </Field>
          <Field label="Motivo">
            <textarea
              required
              maxLength={1000}
              value={form.motivo}
              onChange={(e) => set("motivo", e.target.value)}
            />
          </Field>
          <Field label="Inicio">
            <input
              required
              type="date"
              value={form.fechaInicio}
              onChange={(e) => set("fechaInicio", e.target.value)}
            />
          </Field>
          <Field label="Fin">
            <input
              required
              type="date"
              min={form.fechaInicio}
              value={form.fechaFin}
              onChange={(e) => set("fechaFin", e.target.value)}
            />
          </Field>
        </Editor>
      )}
      <ModuleEmpty module={module} />
      {module.data.map((p) => (
        <article className="panel" key={p.id}>
          <div className="section-title-row">
            <h3>{fullName(p.usuario)}</h3>
            <State value={p.estado} />
          </div>
          <p>{p.motivo}</p>
          <p className="muted">
            {p.fechaInicio} — {p.fechaFin}
          </p>
          <div className="actions-row">
            <button
              className="ghost-button"
              onClick={() => setDetail(detail === p.id ? null : p.id)}
            >
              Ver detalle
            </button>
            {admin && p.estado === "ACTIVA" && (
              <button
                className="secondary-button"
                disabled={module.pending}
                onClick={() => setConfirm(p.id)}
              >
                Cancelar penalización
              </button>
            )}
          </div>
          {detail === p.id && (
            <div className="mini-card">
              <p>
                Registro #{p.id} · {p.usuario.email}
              </p>
              <p>Inicio: {p.fechaInicio}</p>
              <p>Vencimiento: {p.fechaFin}</p>
            </div>
          )}
          {confirm === p.id && (
            <div className="actions-row">
              <p>¿Cancelar esta penalización?</p>
              <button
                className="primary-button"
                disabled={module.pending}
                onClick={async () => {
                  if (
                    await module.act(
                      () => modulesService.cancelPenalty(p.id),
                      "Penalización cancelada.",
                    )
                  )
                    setConfirm(null);
                }}
              >
                Sí, cancelar
              </button>
              <button className="ghost-button" onClick={() => setConfirm(null)}>
                Mantener
              </button>
            </div>
          )}
        </article>
      ))}
    </div>
  );
}
