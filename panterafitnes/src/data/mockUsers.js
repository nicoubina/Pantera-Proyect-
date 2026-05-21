export const ROLES = {
  CLIENTE: "CLIENTE",
  PROFESOR: "PROFESOR",
  ADMINISTRADOR: "ADMINISTRADOR"
};

export const MEMBRESIAS = {
  ACTIVA: "ACTIVA",
  VENCIDA: "VENCIDA"
};

export const mockUsers = [
  {
    id: "cliente-activo-001",
    nombre: "Cliente Activo",
    email: "cliente@pantera.com",
    password: "123456",
    rol: ROLES.CLIENTE,
    membresia: MEMBRESIAS.ACTIVA
  },
  {
    id: "cliente-vencido-001",
    nombre: "Cliente Vencido",
    email: "vencido@pantera.com",
    password: "123456",
    rol: ROLES.CLIENTE,
    membresia: MEMBRESIAS.VENCIDA
  },
  {
    id: "profesor-001",
    nombre: "Profesor Pantera",
    email: "profesor@pantera.com",
    password: "123456",
    rol: ROLES.PROFESOR,
    membresia: null
  },
  {
    id: "admin-001",
    nombre: "Administrador Pantera",
    email: "admin@pantera.com",
    password: "123456",
    rol: ROLES.ADMINISTRADOR,
    membresia: null
  }
];
