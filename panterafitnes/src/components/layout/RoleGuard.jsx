"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { roleHomePaths } from "@/components/layout/navigation";
import AppLayout from "@/components/layout/AppLayout";

export default function RoleGuard({ allowedRole, children }) {
  const router = useRouter();
  const { user, loading } = useAuth();

  useEffect(() => {
    if (loading) {
      return;
    }

    if (!user) {
      router.replace("/login");
      return;
    }

    if (user.rol !== allowedRole) {
      router.replace(roleHomePaths[user.rol] || "/login");
    }
  }, [allowedRole, loading, router, user]);

  if (loading || !user || user.rol !== allowedRole) {
    return (
      <main className="auth-screen">
        <section className="auth-card">
          <p className="eyebrow">Pantera Fitness</p>
          <h1>Validando sesion...</h1>
        </section>
      </main>
    );
  }

  return <AppLayout>{children}</AppLayout>;
}
