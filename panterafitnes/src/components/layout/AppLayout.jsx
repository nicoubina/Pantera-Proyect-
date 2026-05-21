"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import AlertBar from "@/components/layout/AlertBar";
import FeedbackToast from "@/components/layout/FeedbackToast";
import { getNavigationItems, getRoleLabel } from "@/components/layout/navigation";
import NotificationBell from "@/components/notificaciones/NotificationBell";
import { useAuth } from "@/context/AuthContext";

export default function AppLayout({ children }) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, logout } = useAuth();
  const navItems = getNavigationItems(user.rol);

  function handleLogout() {
    logout();
    router.replace("/login");
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand-block">
          <span className="brand-mark">PF</span>
          <div>
            <strong>Pantera Fitness</strong>
            <small>MVP Frontend</small>
          </div>
        </div>

        <nav className="side-nav" aria-label="Navegacion principal">
          {navItems.map((item) => (
            <Link
              className={pathname === item.href ? "active" : ""}
              href={item.href}
              key={item.href}
            >
              {item.label}
            </Link>
          ))}
        </nav>
      </aside>

      <div className="content-shell">
        <header className="top-header">
          <div>
            <p className="eyebrow">Pantera Fitness</p>
            <h1>{getRoleLabel(user.rol)}</h1>
          </div>
          <div className="header-actions">
            <NotificationBell />
            <div className="user-chip">
              <span>{user.nombre}</span>
              <small>{getRoleLabel(user.rol)}</small>
            </div>
            <button className="ghost-button" type="button" onClick={handleLogout}>
              Cerrar sesion
            </button>
          </div>
        </header>

        <AlertBar />
        <main className="page-content">{children}</main>
      </div>

      <nav className="bottom-nav" aria-label="Navegacion movil">
        {navItems.map((item) => (
          <Link
            className={pathname === item.href ? "active" : ""}
            href={item.href}
            key={item.href}
          >
            {item.label}
          </Link>
        ))}
      </nav>
      <FeedbackToast />
    </div>
  );
}
