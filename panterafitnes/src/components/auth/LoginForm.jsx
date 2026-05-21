"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { roleHomePaths } from "@/components/layout/navigation";

const testUsers = [
  "cliente@pantera.com",
  "vencido@pantera.com",
  "profesor@pantera.com",
  "admin@pantera.com"
];

export default function LoginForm() {
  const router = useRouter();
  const { user, loading, login } = useAuth();
  const [email, setEmail] = useState("cliente@pantera.com");
  const [password, setPassword] = useState("123456");
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
      const authenticatedUser = login(email, password);
      router.replace(roleHomePaths[authenticatedUser.rol] || "/cliente");
    } catch (loginError) {
      setError(loginError.message);
    }
  }

  return (
    <main className="auth-screen">
      <section className="auth-card">
        <p className="eyebrow">Pantera Fitness</p>
        <h1>Ingresar al MVP</h1>
        <p className="muted">
          Acceso simulado con datos mockeados. No consume backend ni endpoints reales.
        </p>

        <form className="auth-form" onSubmit={handleSubmit}>
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
            Iniciar sesion
          </button>
        </form>

        <div className="test-users">
          <p>Usuarios de prueba, password 123456:</p>
          {testUsers.map((testEmail) => (
            <button
              className="ghost-button small"
              key={testEmail}
              type="button"
              onClick={() => {
                setEmail(testEmail);
                setPassword("123456");
              }}
            >
              {testEmail}
            </button>
          ))}
        </div>

        <p className="auth-link">
          No tenes usuario? <Link href="/registro">Registrate como cliente</Link>
        </p>
      </section>
    </main>
  );
}
