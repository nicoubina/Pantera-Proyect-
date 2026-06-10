"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { roleHomePaths } from "@/components/layout/navigation";
import { useAuth } from "@/context/AuthContext";

function getStrengthLevel(password) {
  if (password.length >= 10) {
    return "strong";
  }

  if (password.length >= 6) {
    return "medium";
  }

  return "weak";
}

const STRENGTH_LABELS = {
  weak: "Débil",
  medium: "Media",
  strong: "Fuerte"
};

const STRENGTH_COLORS = {
  weak: "var(--color-red)",
  medium: "var(--color-yellow)",
  strong: "var(--color-green)"
};

export default function RegisterForm() {
  const router = useRouter();
  const { user, loading, register } = useAuth();
  const [nombre, setNombre] = useState("");
  const [apellido, setApellido] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [strength, setStrength] = useState("weak");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!loading && user) {
      router.replace(roleHomePaths[user.rol] || "/cliente");
    }
  }, [loading, router, user]);

  function handlePasswordChange(value) {
    setPassword(value);
    setStrength(getStrengthLevel(value));
  }

  function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setIsLoading(true);

    const fullName = apellido.trim() ? `${nombre} ${apellido}`.trim() : nombre;

    window.setTimeout(() => {
      try {
        const registeredUser = register({ nombre: fullName, email, password });
        router.replace(roleHomePaths[registeredUser.rol] || "/cliente");
      } catch (registerError) {
        setError(registerError.message);
        setIsLoading(false);
      }
    }, 300);
  }

  const activeBars = password.length === 0 ? 0 : strength === "weak" ? 1 : strength === "medium" ? 2 : 3;

  return (
    <main className="auth-screen">
      <div className="bg-glow" />
      <section className="auth-card stitch-auth" style={{ position: "relative", zIndex: 1 }}>
        <header
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            textAlign: "center",
            marginBottom: 32
          }}
        >
          <div
            className="brand-mark"
            style={{ marginBottom: 20, boxShadow: "0 0 20px rgba(255,122,26,0.3)" }}
          >
            PF
          </div>
          <h1 className="font-display" style={{ fontSize: "2rem", marginBottom: 8 }}>
            Pantera Fitness
          </h1>
          <p className="muted">Entrená con propósito</p>
        </header>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="register-name-grid" style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
            <label>
              Nombre
              <input
                value={nombre}
                onChange={(event) => setNombre(event.target.value)}
                required
              />
            </label>
            <label>
              Apellido (Opcional)
              <input value={apellido} onChange={(event) => setApellido(event.target.value)} />
            </label>
          </div>
          <label>
            Email
            <div style={{ position: "relative" }}>
              <span
                className="material-symbols-outlined"
                style={{
                  position: "absolute",
                  left: 14,
                  top: "50%",
                  transform: "translateY(-50%)",
                  color: "var(--color-muted)",
                  fontSize: "20px"
                }}
              >
                mail
              </span>
              <input
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                style={{ paddingLeft: 44 }}
                required
              />
            </div>
          </label>
          <label>
            Password
            <div style={{ position: "relative" }}>
              <span
                className="material-symbols-outlined"
                style={{
                  position: "absolute",
                  left: 14,
                  top: "50%",
                  transform: "translateY(-50%)",
                  color: "var(--color-muted)",
                  fontSize: "20px"
                }}
              >
                lock
              </span>
              <input
                type="password"
                value={password}
                onChange={(event) => handlePasswordChange(event.target.value)}
                style={{ paddingLeft: 44 }}
                required
              />
            </div>
            {password.length > 0 ? (
              <div style={{ display: "flex", alignItems: "center", gap: 10, marginTop: 4 }}>
                <div style={{ display: "flex", gap: 6, flex: 1 }}>
                  {[1, 2, 3].map((bar) => (
                    <div
                      key={bar}
                      className={`strength-bar ${bar <= activeBars ? strength : ""}`}
                    />
                  ))}
                </div>
                <span style={{ fontSize: "0.75rem", fontWeight: 800, color: STRENGTH_COLORS[strength] }}>
                  {STRENGTH_LABELS[strength]}
                </span>
              </div>
            ) : null}
          </label>
          {error ? <p className="form-error">{error}</p> : null}
          <button className="primary-button" type="submit" disabled={isLoading}>
            {isLoading ? (
              <span style={{ display: "inline-flex", alignItems: "center", gap: 8 }}>
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  style={{ animation: "spin 0.8s linear infinite" }}
                >
                  <circle
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="3"
                    strokeOpacity="0.25"
                  />
                  <path
                    d="M22 12a10 10 0 0 0-10-10"
                    stroke="currentColor"
                    strokeWidth="3"
                    strokeLinecap="round"
                  />
                </svg>
                Creando cuenta...
              </span>
            ) : (
              "Crear cuenta"
            )}
          </button>
        </form>

        <p className="auth-link">
          Ya tenes usuario? <Link href="/login">Ingresar</Link>
        </p>
      </section>
      <style>{`
        @keyframes spin {
          to { transform: rotate(360deg); }
        }
      `}</style>
    </main>
  );
}
