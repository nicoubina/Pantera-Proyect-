import { MEMBRESIAS, mockUsers, ROLES } from "@/data/mockUsers";
import { readStorage, removeStorage, writeStorage } from "@/services/storageService";

const REGISTERED_USERS_KEY = "pantera_registered_users";
const SESSION_KEY = "pantera_current_user";

function publicUser(user) {
  const { password, ...safeUser } = user;
  return safeUser;
}

function getRegisteredUsers() {
  return readStorage(REGISTERED_USERS_KEY, []);
}

function saveRegisteredUsers(users) {
  writeStorage(REGISTERED_USERS_KEY, users);
}

export const authService = {
  getAllUsers() {
    return [...mockUsers, ...getRegisteredUsers()].map(publicUser);
  },

  getCurrentUser() {
    return readStorage(SESSION_KEY, null);
  },

  login(email, password) {
    const normalizedEmail = email.trim().toLowerCase();
    const users = [...mockUsers, ...getRegisteredUsers()];
    const user = users.find(
      (candidate) =>
        candidate.email.toLowerCase() === normalizedEmail &&
        candidate.password === password
    );

    if (!user) {
      throw new Error("Email o password incorrectos.");
    }

    const safeUser = publicUser(user);
    writeStorage(SESSION_KEY, safeUser);
    return safeUser;
  },

  register({ nombre, email, password }) {
    const normalizedEmail = email.trim().toLowerCase();
    const users = [...mockUsers, ...getRegisteredUsers()];
    const exists = users.some((user) => user.email.toLowerCase() === normalizedEmail);

    if (exists) {
      throw new Error("Ya existe un usuario registrado con ese email.");
    }

    if (password.length < 6) {
      throw new Error("El password debe tener al menos 6 caracteres.");
    }

    const newUser = {
      id: `cliente-${Date.now()}`,
      nombre: nombre.trim(),
      email: normalizedEmail,
      password,
      rol: ROLES.CLIENTE,
      membresia: MEMBRESIAS.ACTIVA
    };

    saveRegisteredUsers([...getRegisteredUsers(), newUser]);
    const safeUser = publicUser(newUser);
    writeStorage(SESSION_KEY, safeUser);
    return safeUser;
  },

  logout() {
    removeStorage(SESSION_KEY);
  }
};
