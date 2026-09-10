"use client";

import { createContext, useContext, useEffect, useRef, useState } from "react";
import { authService } from "@/services/authService";
import { SESSION_EXPIRED_EVENT, TOKEN_KEY } from "@/services/apiClient";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [sessionError, setSessionError] = useState("");
  const sessionVersion = useRef(0);

  useEffect(() => {
    let active = true;
    async function restore() {
      const version = ++sessionVersion.current;
      const isCurrent = () => active && version === sessionVersion.current;
      setLoading(true);
      setSessionError("");
      if (!authService.hasSession()) {
        if (isCurrent()) { setUser(null); setLoading(false); }
        return;
      }
      try {
        const profile = await authService.getProfile();
        if (isCurrent()) setUser(profile);
      } catch (error) {
        if (isCurrent()) {
          setUser(null);
          setSessionError(error.message);
        }
      } finally {
        if (isCurrent()) setLoading(false);
      }
    }
    function expired() {
      sessionVersion.current += 1;
      setUser(null);
      setLoading(false);
      setSessionError("Tu sesión venció. Volvé a iniciar sesión.");
    }
    function storageChanged(event) {
      if (event.key === TOKEN_KEY || event.key === null) {
        setUser(null);
        restore();
      }
    }
    restore();
    window.addEventListener(SESSION_EXPIRED_EVENT, expired);
    window.addEventListener("storage", storageChanged);
    return () => {
      active = false;
      window.removeEventListener(SESSION_EXPIRED_EVENT, expired);
      window.removeEventListener("storage", storageChanged);
    };
  }, []);

  const value = {
    user, loading, sessionError,
    async login(email, password) {
      sessionVersion.current += 1;
      setLoading(false);
      const authenticatedUser = await authService.login(email, password);
      setSessionError("");
      setUser(authenticatedUser);
      return authenticatedUser;
    },
    async register(data) {
      sessionVersion.current += 1;
      setLoading(false);
      const registeredUser = await authService.register(data);
      setSessionError("");
      setUser(registeredUser);
      return registeredUser;
    },
    logout() {
      sessionVersion.current += 1;
      setLoading(false);
      authService.logout();
      setUser(null);
      setSessionError("");
    }
  };
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth debe usarse dentro de AuthProvider.");
  return context;
}
