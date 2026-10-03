"use client";
import { useEffect, useState } from "react";
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
import { modulesService } from "@/services/modulesService";

const blankExercise = () => ({
  ejercicioId: "",
  series: 3,
  repeticiones: 10,
  pesoSugerido: 0,
  descanso: 60,
});
export default function RoutinesView() {
  const module = useModule("rutinas");
  const { user } = useAuth();
  const professor = user.rol === "PROFESOR";
  const catalog = useModule("ejercicios");
  const [clients, setClients] = useState([]);
  const [clientsError, setClientsError] = useState("");
  const [form, setForm] = useState(null);
  const [expanded, setExpanded] = useState(null);
  const [exerciseForm, setExerciseForm] = useState(null);
  useEffect(() => {
    if (!professor) return;
    let active = true;
    modulesService
      .list("rutinas/clientes")
      .then((r) => {
        if (active) setClients(r);
      })
      .catch((e) => {
        if (active) setClientsError(e.message);
      });
    return () => {
      active = false;
    };
  }, [professor]);
  const set = (key, value) => setForm((f) => ({ ...f, [key]: value }));
  function changeExercise(index, key, value) {
    setForm((f) => ({
      ...f,
      ejercicios: f.ejercicios.map((e, i) =>
        i === index ? { ...e, [key]: value } : e,
      ),
    }));
  }
  async function save(event) {
    event.preventDefault();
    const body = {
      ...form,
      clienteId: Number(form.clienteId),
      ejercicios: form.ejercicios.map((e) => ({
        ...e,
        ejercicioId: Number(e.ejercicioId),
        series: Number(e.series),
        repeticiones: Number(e.repeticiones),
        pesoSugerido: e.pesoSugerido === "" ? null : Number(e.pesoSugerido),
        descanso: Number(e.descanso),
      })),
    };
    if (
      await module.act(
        () => modulesService.save("rutinas", form.id, body),
        "Rutina guardada y asignada.",
      )
    )
      setForm(null);
  }
  function edit(r) {
    setForm({
      ...r,
      clienteId: r.cliente.id,
      ejercicios: r.ejercicios.map((e) => ({
        ejercicioId: e.ejercicio.id,
        series: e.series,
        repeticiones: e.repeticiones,
        pesoSugerido: e.pesoSugerido ?? "",
        descanso: e.descanso,
      })),
    });
  }
  return (
    <div className="stack">
      <PageHeader
        title={professor ? "Mis rutinas" : "Rutinas asignadas"}
        description="Plan de entrenamiento con ejercicios, series, repeticiones, peso sugerido y descanso."
      />
      <ModuleFeedback module={module} />
      {clientsError && (
        <p role="alert" className="warning-panel">
          {clientsError}
        </p>
      )}
      {professor && (
        <div className="actions-row">
          <button
            className="primary-button"
            onClick={() =>
              setForm({
                nombre: "",
                descripcion: "",
                clienteId: "",
                estado: "ACTIVA",
                ejercicios: [blankExercise()],
              })
            }
          >
            Crear rutina
          </button>
          <button
            className="secondary-button"
            onClick={() => setExerciseForm({ nombre: "", descripcion: "" })}
          >
            Crear ejercicio
          </button>
        </div>
      )}
      {exerciseForm && (
        <Editor
          title="Nuevo ejercicio"
          pending={module.pending}
          onClose={() => setExerciseForm(null)}
          onSubmit={async (e) => {
            e.preventDefault();
            if (
              await module.act(
                () => modulesService.save("ejercicios", null, exerciseForm),
                "Ejercicio creado.",
              )
            ) {
              setExerciseForm(null);
              await catalog.load();
            }
          }}
        >
          <Field label="Nombre">
            <input
              required
              maxLength={255}
              value={exerciseForm.nombre}
              onChange={(e) =>
                setExerciseForm((f) => ({ ...f, nombre: e.target.value }))
              }
            />
          </Field>
          <Field label="Descripción">
            <textarea
              maxLength={1000}
              value={exerciseForm.descripcion}
              onChange={(e) =>
                setExerciseForm((f) => ({ ...f, descripcion: e.target.value }))
              }
            />
          </Field>
        </Editor>
      )}
      {form && (
        <Editor
          title={form.id ? "Editar rutina" : "Crear y asignar rutina"}
          onSubmit={save}
          onClose={() => setForm(null)}
          pending={module.pending}
        >
          <ModuleFeedback module={catalog} />
          <Field label="Nombre">
            <input
              required
              maxLength={255}
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
          <Field label="Cliente relacionado">
            <select
              required
              value={form.clienteId}
              onChange={(e) => set("clienteId", e.target.value)}
            >
              <option value="">Seleccionar cliente</option>
              {clients.map((c) => (
                <option value={c.id} key={c.id}>
                  {fullName(c)} · {c.email}
                </option>
              ))}
            </select>
          </Field>
          {!clients.length && (
            <p className="muted">
              Se muestran clientes inscriptos en tus clases o con una rutina
              propia asignada.
            </p>
          )}
          <Field label="Estado">
            <select
              value={form.estado}
              onChange={(e) => set("estado", e.target.value)}
            >
              {["ACTIVA", "PAUSADA", "FINALIZADA"].map((s) => (
                <option key={s}>{s}</option>
              ))}
            </select>
          </Field>
          {form.ejercicios.map((e, index) => (
            <section className="mini-card" key={index}>
              <h4>Ejercicio {index + 1}</h4>
              <Field label="Ejercicio">
                <select
                  required
                  value={e.ejercicioId}
                  onChange={(ev) =>
                    changeExercise(index, "ejercicioId", ev.target.value)
                  }
                >
                  <option value="">Seleccionar</option>
                  {catalog.data.map((c) => (
                    <option value={c.id} key={c.id}>
                      {c.nombre}
                    </option>
                  ))}
                </select>
              </Field>
              <div className="module-grid">
                {[
                  ["series", "Series", 1],
                  ["repeticiones", "Repeticiones", 1],
                  ["pesoSugerido", "Peso sugerido (kg)", 0],
                  ["descanso", "Descanso (segundos)", 0],
                ].map(([key, label, min]) => (
                  <Field label={label} key={key}>
                    <input
                      required={key !== "pesoSugerido"}
                      type="number"
                      min={min}
                      step={key === "pesoSugerido" ? "0.01" : "1"}
                      value={e[key]}
                      onChange={(ev) =>
                        changeExercise(index, key, ev.target.value)
                      }
                    />
                  </Field>
                ))}
              </div>
              <div className="actions-row">
                <button
                  type="button"
                  className="ghost-button"
                  disabled={form.ejercicios.length <= 1}
                  onClick={() =>
                    set(
                      "ejercicios",
                      form.ejercicios.filter((_, i) => i !== index),
                    )
                  }
                >
                  Quitar
                </button>
                <button
                  type="button"
                  className="ghost-button"
                  disabled={index === 0}
                  onClick={() => {
                    const next = [...form.ejercicios];
                    [next[index - 1], next[index]] = [
                      next[index],
                      next[index - 1],
                    ];
                    set("ejercicios", next);
                  }}
                >
                  Subir
                </button>
              </div>
            </section>
          ))}
          <button
            type="button"
            className="secondary-button"
            onClick={() =>
              set("ejercicios", [...form.ejercicios, blankExercise()])
            }
          >
            Agregar ejercicio
          </button>
        </Editor>
      )}
      <ModuleEmpty module={module} />
      {module.data.map((r) => (
        <article className="panel" key={r.id}>
          <div className="section-title-row">
            <h3>{r.nombre}</h3>
            <State value={r.estado} />
          </div>
          <p>{r.descripcion}</p>
          <p className="muted">
            Profesor: {fullName(r.profesor)} · Cliente: {fullName(r.cliente)}
          </p>
          <div className="actions-row">
            <button
              className="secondary-button"
              onClick={() => setExpanded(expanded === r.id ? null : r.id)}
            >
              Ver detalle
            </button>
            {professor && (
              <button className="ghost-button" onClick={() => edit(r)}>
                Editar / Asignar
              </button>
            )}
          </div>
          {expanded === r.id && (
            <div className="stack">
              {r.ejercicios.map((e) => (
                <section className="mini-card" key={e.id}>
                  <h4>
                    {e.orden}. {e.ejercicio.nombre}
                  </h4>
                  <p>{e.ejercicio.descripcion}</p>
                  <p>
                    {e.series} series · {e.repeticiones} repeticiones ·{" "}
                    {e.pesoSugerido ?? "Sin indicación"} kg · {e.descanso} s de
                    descanso
                  </p>
                </section>
              ))}
            </div>
          )}
        </article>
      ))}
    </div>
  );
}
