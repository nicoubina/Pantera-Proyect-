import { ROLES } from "@/data/mockUsers";

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

  return [
    { label: "Inicio", href: basePath, icon: "home" },
    { label: "Ocupacion", href: `${basePath}/ocupacion`, icon: "monitoring" },
    { label: "Clases", href: `${basePath}/clases`, icon: "fitness_center" },
    { label: reservationsLabel, href: `${basePath}/reservas`, icon: "event_available" },
    { label: "Perfil", href: `${basePath}/perfil`, icon: "person" }
  ];
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
