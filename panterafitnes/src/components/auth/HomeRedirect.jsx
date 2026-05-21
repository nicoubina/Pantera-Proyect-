"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { roleHomePaths } from "@/components/layout/navigation";

export default function HomeRedirect() {
  const router = useRouter();
  const { user, loading } = useAuth();

  useEffect(() => {
    if (loading) {
      return;
    }

    router.replace(user ? roleHomePaths[user.rol] || "/cliente" : "/login");
  }, [loading, router, user]);

  return (
    <main className="auth-screen">
      <section className="auth-card">
        <p className="eyebrow">Pantera Fitness</p>
        <h1>Cargando MVP frontend...</h1>
      </section>
    </main>
  );
}
