"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { roleHomePaths } from "@/components/layout/navigation";
import { useAuth } from "@/context/AuthContext";

export default function RegisterForm() {
  const router = useRouter();
  const { user, loading, register } = useAuth();
  const [nombre, setNombre] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    if (!loading && user) {
      router.replace(roleHomePaths[user.rol] || "/cliente");
    }
  }, [loading, router, user]);

  function handleSubmit(event) {
    event.preventDefault();
    setError("");

    try {
      const registeredUser = register({ nombre, email, password });
      router.replace(roleHomePaths[registeredUser.rol] || "/cliente");
    } catch (registerError) {
      setError(registerError.message);
    }
  }

  return (
    <main className="auth-screen">
      <section className="auth-card">
        <p className="eyebrow">Pantera Fitness</p>
        <h1>Registro de cliente</h1>
        <p className="muted">
          El usuario nuevo se guarda en localStorage con membresia activa simulada.
        </p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Nombre
            <input
              value={nombre}
              onChange={(event) => setNombre(event.target.value)}
              required
            />
          </label>
          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </label>
          {error ? <p className="form-error">{error}</p> : null}
          <button className="primary-button" type="submit">
            Crear cuenta
          </button>
        </form>

        <p className="auth-link">
          Ya tenes usuario? <Link href="/login">Ingresar</Link>
        </p>
      </section>
    </main>
  );
}
