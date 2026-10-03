"use client";
import PageHeader from "@/components/common/PageHeader";
import useModule from "./useModule";
import { ModuleFeedback, State } from "./ModuleUI";

export default function MembershipView() {
  const module = useModule("membresias/mi-membresia");
  const m = module.data;
  return (
    <div className="stack">
      <PageHeader
        title="Mi membresía"
        description="Estado y vigencia de tu membresía."
      />
      <ModuleFeedback module={module} />
      {!module.loading && m.usuarioId && (
        <section className="panel">
          <State value={m.estado} />
          <p>Inicio: {m.fechaInicio || "Sin fecha"}</p>
          <p>Vencimiento: {m.fechaVencimiento || "Sin fecha"}</p>
          <p className="muted">
            Las reservas requieren una membresía activa y vigente. Contactá a
            administración para gestionar el estado.
          </p>
        </section>
      )}
    </div>
  );
}
