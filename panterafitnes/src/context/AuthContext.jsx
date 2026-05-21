"use client";

import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { authService } from "@/services/authService";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setUser(authService.getCurrentUser());
    setLoading(false);
  }, []);

  const value = useMemo(
    () => ({
      user,
      loading,
      login(email, password) {
        const authenticatedUser = authService.login(email, password);
        setUser(authenticatedUser);
        return authenticatedUser;
      },
      register(data) {
        const registeredUser = authService.register(data);
        setUser(registeredUser);
        return registeredUser;
      },
      logout() {
        authService.logout();
        setUser(null);
      }
    }),
    [user, loading]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth debe usarse dentro de AuthProvider.");
  }

  return context;
}
