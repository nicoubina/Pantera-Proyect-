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
    { label: "Inicio", href: basePath },
    { label: "Ocupacion", href: `${basePath}/ocupacion` },
    { label: "Clases", href: `${basePath}/clases` },
    { label: reservationsLabel, href: `${basePath}/reservas` },
    { label: "Perfil", href: `${basePath}/perfil` }
  ];
}
