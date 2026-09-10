import { apiRequest, clearSession, SESSION_KEY, TOKEN_KEY } from "./apiClient";
import { readStorage, writeStorage } from "./storageService";
import { mapUser } from "./mappers";

function saveAuth(response) {
  const user = mapUser(response.usuario);
  writeStorage(TOKEN_KEY, response.token);
  writeStorage(SESSION_KEY, user);
  return user;
}

export const authService = {
  getCurrentUser() {
    return readStorage(TOKEN_KEY, null) ? readStorage(SESSION_KEY, null) : null;
  },
  hasSession() { return Boolean(readStorage(TOKEN_KEY, null)); },
  async getProfile() {
    const token = readStorage(TOKEN_KEY, null);
    const user = mapUser(await apiRequest("/api/usuarios/me"));
    if (token === readStorage(TOKEN_KEY, null)) writeStorage(SESSION_KEY, user);
    return user;
  },
  async login(email, password) {
    return saveAuth(await apiRequest("/api/auth/login", {
      method: "POST", auth: false, body: { email: email.trim().toLowerCase(), password }
    }));
  },
  async register({ nombre, apellido, email, password }) {
    return saveAuth(await apiRequest("/api/auth/registro", {
      method: "POST", auth: false,
      body: { nombre: nombre.trim(), apellido: apellido?.trim(), email: email.trim().toLowerCase(), password }
    }));
  },
  logout: clearSession
};
