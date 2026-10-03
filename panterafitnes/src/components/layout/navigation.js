import { ROLES } from "@/data/constants";

export const roleHomePaths = {
  [ROLES.CLIENTE]: "/cliente",
  [ROLES.PROFESOR]: "/profesor",
  [ROLES.ADMINISTRADOR]: "/admin"
};

export function getRoleLabel(role) {
  const labels = {
    [ROLES.CLIENTE]: "Cliente",
    [ROLES.PROFESOR]: "Profesor",
    [ROLES.ADMINISTRADOR]: "Administrador"
  };

  return labels[role] || role;
}

export function getNavigationItems(role) {
  const basePath = roleHomePaths[role] || "/cliente";
  const reservationsLabel = role === ROLES.CLIENTE ? "Mis reservas" : "Reservas";

  const items = [
    { label: role === ROLES.ADMINISTRADOR ? "Dashboard" : "Inicio", href: basePath, icon: "home" },
    { label: "Ocupacion", href: `${basePath}/ocupacion`, icon: "monitoring" },
    { label: "Clases", href: `${basePath}/clases`, icon: "fitness_center" },
    { label: reservationsLabel, href: `${basePath}/reservas`, icon: "event_available" },
  ];
  const modules = role === ROLES.ADMINISTRADOR
    ? [["Usuarios", "usuarios", "group"], ["Profesores", "profesores", "sports"], ["Horarios", "horarios", "schedule"], ["Asistencias", "asistencias", "checklist"], ["Lista de espera", "lista-espera", "hourglass_empty"], ["Membresías", "membresias", "badge"], ["Penalizaciones", "penalizaciones", "gavel"], ["Rutinas", "rutinas", "exercise"], ["Alertas", "alertas", "warning"]]
    : role === ROLES.CLIENTE
      ? [["Asistencias", "asistencias", "checklist"], ["Lista de espera", "lista-espera", "hourglass_empty"], ["Membresía", "membresia", "badge"], ["Penalizaciones", "penalizaciones", "gavel"], ["Mis rutinas", "rutinas", "exercise"], ["Mi QR", "qr", "qr_code"]]
      : [["Asistencias", "asistencias", "checklist"], ["Lista de espera", "lista-espera", "hourglass_empty"], ["Rutinas", "rutinas", "exercise"]];
  return [...items, ...modules.map(([label, path, icon]) => ({ label, href: `${basePath}/${path}`, icon })),
    { label: "Notificaciones", href: `${basePath}/notificaciones`, icon: "notifications" },
    { label: "Perfil", href: `${basePath}/perfil`, icon: "person" }];
}

export const pathLabels = {
  "/cliente": "Inicio",
  "/cliente/ocupacion": "Ocupación",
  "/cliente/clases": "Clases",
  "/cliente/reservas": "Mis Reservas",
  "/cliente/perfil": "Perfil",
  "/profesor": "Inicio",
  "/profesor/ocupacion": "Ocupación",
  "/profesor/clases": "Clases",
  "/profesor/reservas": "Reservas",
  "/profesor/perfil": "Perfil",
  "/admin": "Dashboard",
  "/admin/ocupacion": "Ocupación",
  "/admin/clases": "Gestión de Clases",
  "/admin/reservas": "Reservas",
  "/admin/perfil": "Perfil"
};

Object.keys(roleHomePaths).forEach(role => getNavigationItems(role).forEach(item => { pathLabels[item.href] = item.label; }));
