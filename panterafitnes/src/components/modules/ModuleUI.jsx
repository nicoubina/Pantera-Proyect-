"use client";

import EmptyState from "@/components/common/EmptyState";
import StatusPill from "@/components/common/StatusPill";

export const fullName = (u) =>
  [u?.nombre, u?.apellido].filter(Boolean).join(" ");
export const scheduleLabel = (h) =>
  `${h.claseGimnasio.nombre} · ${h.fecha} ${h.horaInicio.slice(0, 5)}`;
export function ModuleFeedback({ module }) {
  return (
    <>
      {module.loading && (
        <section className="panel" role="status">
          Cargando...
        </section>
      )}
      {module.error && (
        <section className="warning-panel" role="alert">
          <p>{module.error}</p>
          <button className="ghost-button" onClick={module.load}>
            Reintentar
          </button>
        </section>
      )}
      {module.success && (
        <p className="module-success" role="status">
          {module.success}
        </p>
      )}
    </>
  );
}
export function State({ value }) {
  return (
    <StatusPill
      tone={
        ["ACTIVA", "ASISTIDA", "CONFIRMADA"].includes(value)
          ? "success"
          : ["AUSENTE", "VENCIDA", "SUSPENDIDA"].includes(value)
            ? "danger"
            : "neutral"
      }
    >
      {value}
    </StatusPill>
  );
}
export function ModuleEmpty({ module }) {
  return !module.loading && !module.error && !module.data.length ? (
    <EmptyState
      title="Sin registros"
      description="Los registros aparecerán aquí cuando se carguen en el gimnasio."
    />
  ) : null;
}
export function Field({ label, children }) {
  return (
    <label>
      {label}
      {children}
    </label>
  );
}
export function Editor({ title, children, onSubmit, onClose, pending }) {
  return (
    <section className="panel module-editor" aria-label={title}>
      <div className="section-title-row">
        <h3>{title}</h3>
        <button
          type="button"
          className="ghost-button"
          disabled={pending}
          onClick={onClose}
        >
          Cerrar
        </button>
      </div>
      <form className="stack" onSubmit={onSubmit}>
        <fieldset disabled={pending} className="module-fields">
          {children}
        </fieldset>
        <div className="actions-row">
          <button className="primary-button" disabled={pending}>
            {pending ? "Guardando..." : "Guardar"}
          </button>
          <button
            type="button"
            className="ghost-button"
            disabled={pending}
            onClick={onClose}
          >
            Cancelar
          </button>
        </div>
      </form>
    </section>
  );
}
