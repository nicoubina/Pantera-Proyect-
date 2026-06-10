"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import FeedbackToast from "@/components/layout/FeedbackToast";
import { getNavigationItems, getRoleLabel, pathLabels } from "@/components/layout/navigation";
import NotificationBell from "@/components/notificaciones/NotificationBell";
import { useAuth } from "@/context/AuthContext";
import { useAppData } from "@/context/AppDataContext";

export default function AppLayout({ children }) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, logout } = useAuth();
  const { occupancy } = useAppData();
  const navItems = getNavigationItems(user.rol);
  const [scrolled, setScrolled] = useState(false);
  const [occOpen, setOccOpen] = useState(false);
  const occRef = useRef(null);

  function handleLogout() {
    logout();
    router.replace("/login");
  }

  useEffect(() => {
    function handleScroll() {
      setScrolled(window.scrollY > 20);
    }

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  useEffect(() => {
    function handleClickOutside(event) {
      if (occRef.current && !occRef.current.contains(event.target)) {
        setOccOpen(false);
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const initials = `${user.nombre?.[0] || ""}${user.nombre?.split(" ")[1]?.[0] || ""}`;

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand-block">
          <span className="brand-mark">PF</span>
          <div className="brand-text">
            <strong>Pantera Fitness</strong>
          </div>
        </div>

        <nav className="side-nav" aria-label="Navegacion principal">
          {navItems.map((item) => (
            <Link
              className={pathname === item.href ? "active" : ""}
              href={item.href}
              key={item.href}
            >
              <span className="material-symbols-outlined">{item.icon}</span>
              <span className="nav-label">{item.label}</span>
            </Link>
          ))}
        </nav>

        <div className="session-block">
          <div className="avatar-initials">{initials}</div>
          <div className="session-info nav-label">
            <span>{user.nombre}</span>
            <small>{getRoleLabel(user.rol)}</small>
          </div>
        </div>
        <button className="ghost-button" type="button" onClick={handleLogout}>
          <span className="material-symbols-outlined">logout</span>
          <span className="nav-label">Cerrar sesion</span>
        </button>
      </aside>

      <div className="content-shell">
        <header className={`top-header ${scrolled ? "header-scrolled" : ""}`}>
          <div>
            <p className="eyebrow">Pantera Fitness</p>
            <h2 className="font-display page-title">{pathLabels[pathname] || getRoleLabel(user.rol)}</h2>
          </div>
          <div className="header-actions">
            <div className="live-occ-shell" ref={occRef}>
              <button className="live-occ-btn" type="button" onClick={() => setOccOpen((open) => !open)}>
                <span className="status-dot orange pulsing" />
                <span>Live Gym</span>
              </button>
              {occOpen ? (
                <div className="live-occ-popup">
                  <div className="live-occ-header">
                    <span>Ocupación actual</span>
                    <span style={{ color: "var(--color-orange)", fontSize: "0.72rem", fontWeight: 800 }}>
                      En vivo
                    </span>
                  </div>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end" }}>
                    <strong style={{ fontSize: "2rem", color: "var(--color-orange)" }}>
                      {occupancy?.total.porcentaje ?? 0}%
                    </strong>
                    <span className="muted" style={{ fontSize: "0.78rem" }}>
                      {occupancy?.total.personas ?? 0} personas
                    </span>
                  </div>
                  <div className="progress-track" style={{ marginTop: 8 }}>
                    <span
                      className="occupancy-bar"
                      style={{ width: `${occupancy?.total.porcentaje ?? 0}%` }}
                    />
                  </div>
                  <p className="muted" style={{ fontSize: "0.78rem", marginTop: 8 }}>
                    {occupancy?.total.estado}
                  </p>
                </div>
              ) : null}
            </div>
            <NotificationBell />
            <div className="user-chip">
              <span>{user.nombre}</span>
              <small>{getRoleLabel(user.rol)}</small>
            </div>
          </div>
        </header>

        <main className="page-content">{children}</main>
      </div>

      <nav className="bottom-nav" aria-label="Navegacion movil">
        {navItems.map((item) => (
          <Link
            className={pathname === item.href ? "active" : ""}
            href={item.href}
            key={item.href}
          >
            <span
              className="material-symbols-outlined"
              style={{ color: pathname === item.href ? "var(--color-orange)" : "inherit" }}
            >
              {item.icon}
            </span>
            <span>{item.label}</span>
          </Link>
        ))}
      </nav>
      <FeedbackToast />
    </div>
  );
}
