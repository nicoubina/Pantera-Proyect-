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
import { modulesService } from "@/services/modulesService";

export default function UsersView({ memberships = false, professors = false }) {
  const module = useModule("usuarios");
  const classes = useModule("clases");
  const [search, setSearch] = useState("");
  const [form, setForm] = useState(null);
  const [membership, setMembership] = useState(null);
  const [detail, setDetail] = useState(null);
  const set = (key, value) => setForm((f) => ({ ...f, [key]: value }));
  async function save(e) {
    e.preventDefault();
    const body = { ...form };
    if (!body.password) delete body.password;
    if (await module.act(() => modulesService.save("usuarios", form.id, body)))
      setForm(null);
  }
  async function saveMembership(e) {
    e.preventDefault();
    const body = {
      estadoMembresia: membership.estadoMembresia,
      fechaInicioMembresia: membership.fechaInicioMembresia || null,
      fechaVencimientoMembresia: membership.fechaVencimientoMembresia || null,
    };
    if (
      await module.act(
        () => modulesService.membership(membership.id, body),
        "Membresía actualizada.",
      )
    )
      setMembership(null);
  }
  const rows = module.data.filter(
    (u) =>
      (!professors || u.rol === "PROFESOR") &&
      (!memberships || u.rol === "CLIENTE") &&
      `${fullName(u)} ${u.email} ${u.rol} ${u.estadoMembresia}`
        .toLowerCase()
        .includes(search.toLowerCase()),
  );
  return (
    <div className="stack">
      <PageHeader
        title={
          memberships
            ? "Gestión de membresías"
            : professors
              ? "Profesores"
              : "Gestión de usuarios"
        }
        description="Cuentas, roles, estado y datos registrados en el gimnasio."
      />
      <ModuleFeedback module={module} />
      <label>
        Buscar nombre, email, rol o membresía
        <input value={search} onChange={(e) => setSearch(e.target.value)} />
      </label>
      {!memberships && (
        <button
          className="primary-button"
          onClick={() =>
            setForm({
              nombre: "",
              apellido: "",
              email: "",
              password: "",
              rol: professors ? "PROFESOR" : "CLIENTE",
              activo: true,
            })
          }
        >
          Crear {professors ? "profesor" : "usuario"}
        </button>
      )}
      {form && (
        <Editor
          title={form.id ? "Editar usuario" : "Nuevo usuario"}
          onSubmit={save}
          onClose={() => setForm(null)}
          pending={module.pending}
        >
          {[
            ["nombre", "Nombre"],
            ["apellido", "Apellido (opcional)"],
            ["email", "Email"],
            [
              "password",
              form.id ? "Nueva contraseña (opcional)" : "Contraseña",
            ],
          ].map(([key, label]) => (
            <Field key={key} label={label}>
              <input
                type={
                  key === "email"
                    ? "email"
                    : key === "password"
                      ? "password"
                      : "text"
                }
                required={
                  key !== "apellido" && (key !== "password" || !form.id)
                }
                minLength={key === "password" ? 6 : undefined}
                maxLength={key === "password" ? 72 : 255}
                value={form[key] || ""}
                onChange={(e) => set(key, e.target.value)}
              />
            </Field>
          ))}
          <Field label="Rol">
            <select
              value={form.rol}
              onChange={(e) => set("rol", e.target.value)}
            >
              {["CLIENTE", "PROFESOR", "ADMINISTRADOR"].map((r) => (
                <option key={r}>{r}</option>
              ))}
            </select>
          </Field>
          <Field label="Cuenta">
            <select
              value={String(form.activo)}
              onChange={(e) => set("activo", e.target.value === "true")}
            >
              <option value="true">Activa</option>
              <option value="false">Inactiva</option>
            </select>
          </Field>
        </Editor>
      )}
      {membership && (
        <Editor
          title={`Membresía de ${fullName(membership)}`}
          onSubmit={saveMembership}
          onClose={() => setMembership(null)}
          pending={module.pending}
        >
          <Field label="Estado">
            <select
              value={membership.estadoMembresia}
              onChange={(e) =>
                setMembership((f) => ({
                  ...f,
                  estadoMembresia: e.target.value,
                }))
              }
            >
              {["ACTIVA", "VENCIDA", "SUSPENDIDA", "PENDIENTE"].map((s) => (
                <option key={s}>{s}</option>
              ))}
            </select>
          </Field>
          {[
            ["fechaInicioMembresia", "Inicio"],
            ["fechaVencimientoMembresia", "Vencimiento"],
          ].map(([key, label]) => (
            <Field key={key} label={label}>
              <input
                type="date"
                required={membership.estadoMembresia === "ACTIVA"}
                min={
                  key === "fechaVencimientoMembresia"
                    ? membership.fechaInicioMembresia || undefined
                    : undefined
                }
                value={membership[key] || ""}
                onChange={(e) =>
                  setMembership((f) => ({ ...f, [key]: e.target.value }))
                }
              />
            </Field>
          ))}
        </Editor>
      )}
      <ModuleEmpty module={module} />
      {!module.loading && module.data.length > 0 && !rows.length && (
        <p className="panel">Sin resultados para este filtro.</p>
      )}
      {rows.map((u) => (
        <article className="panel" key={u.id}>
          <div className="section-title-row">
            <h3>{fullName(u)}</h3>
            <span className="status-pill">
              {u.rol} · {u.activo ? "Activo" : "Inactivo"}
            </span>
          </div>
          <p>{u.email}</p>
          {u.rol === "CLIENTE" && (
            <>
              <State value={u.estadoMembresia} />
              <p className="muted">
                Membresía: {u.fechaInicioMembresia || "Sin fecha"} —{" "}
                {u.fechaVencimientoMembresia || "Sin fecha"}
              </p>
            </>
          )}
          <div className="actions-row">
            <button
              className="ghost-button"
              onClick={() => setDetail(detail === u.id ? null : u.id)}
            >
              Ver detalle
            </button>
            {!memberships && (
              <button
                className="secondary-button"
                onClick={() => setForm({ ...u, password: "" })}
              >
                Editar cuenta / Rol
              </button>
            )}
            {u.rol === "CLIENTE" && (
              <button
                className="secondary-button"
                onClick={() => setMembership(u)}
              >
                Gestionar membresía
              </button>
            )}
          </div>
          {detail === u.id && (
            <section className="mini-card">
              <p>
                Cuenta #{u.id} · Alta: {u.fechaCreacion?.replace("T", " ")}
              </p>
              {u.rol === "PROFESOR" && (
                <>
                  <h4>Clases asignadas</h4>
                  <ModuleFeedback module={classes} />
                  {classes.data
                    .filter((c) => c.profesor.id === u.id)
                    .map((c) => (
                      <p key={c.id}>
                        {c.nombre} · {c.activa ? "Activa" : "Inactiva"}
                      </p>
                    ))}
                </>
              )}
            </section>
          )}
        </article>
      ))}
    </div>
  );
}
