"use client";

import { AppDataProvider } from "@/context/AppDataContext";
import { AuthProvider } from "@/context/AuthContext";

export function Providers({ children }) {
  return (
    <AuthProvider>
      <AppDataProvider>{children}</AppDataProvider>
    </AuthProvider>
  );
}
