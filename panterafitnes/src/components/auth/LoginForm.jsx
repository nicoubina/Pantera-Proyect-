"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { roleHomePaths } from "@/components/layout/navigation";

export default function LoginForm() {
  const router = useRouter();
  const { user, loading, login, sessionError } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (new URLSearchParams(window.location.search).get("sesion") === "expirada") {
      setError("Tu sesión venció. Volvé a iniciar sesión.");
    }
  }, []);

  useEffect(() => {
    if (!loading && user) {
      router.replace(roleHomePaths[user.rol] || "/cliente");
    }
  }, [loading, router, user]);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setIsLoading(true);

    try {
      const authenticatedUser = await login(email, password);
      router.replace(roleHomePaths[authenticatedUser.rol] || "/cliente");
    } catch (loginError) {
      setError(loginError.message);
    } finally {
      setIsLoading(false);
    }
  }

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
                onChange={(event) => setPassword(event.target.value)}
                style={{ paddingLeft: 44 }}
                required
              />
            </div>
          </label>
          {error || sessionError ? <p className="form-error" role="alert">{error || sessionError}</p> : null}
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
                Ingresando...
              </span>
            ) : (
              <span style={{ display: "inline-flex", alignItems: "center", gap: 8 }}>
                Iniciar sesion
                <span className="material-symbols-outlined" style={{ fontSize: "18px" }}>
                  arrow_forward
                </span>
              </span>
            )}
          </button>
        </form>

        <p className="auth-link">
          No tenes usuario? <Link href="/registro">Registrate como cliente</Link>
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
