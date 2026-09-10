import { readStorage, removeStorage } from "./storageService";

export const TOKEN_KEY = "pantera_jwt";
export const SESSION_KEY = "pantera_current_user";
export const SESSION_EXPIRED_EVENT = "pantera:session-expired";
const API_URL = (process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080").trim().replace(/\/+$/, "");
const API_URL_FALLBACK = API_URL.replace("localhost", "127.0.0.1");

export function clearSession() {
  removeStorage(TOKEN_KEY);
  removeStorage(SESSION_KEY);
}

export class ApiError extends Error {
  constructor(message, status = 0) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

export async function apiRequest(path, { method = "GET", body, auth = true } = {}) {
  const token = auth ? readStorage(TOKEN_KEY, null) : null;
  const headers = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (token) headers.Authorization = `Bearer ${token}`;
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 15000);
  let response;
  let data;
  try {
    const request = (baseUrl) => fetch(`${baseUrl}${path}`, {
      method, headers, body: body === undefined ? undefined : JSON.stringify(body),
      cache: "no-store", signal: controller.signal
    });
    try {
      response = await request(API_URL);
    } catch (firstError) {
      // Some Windows/browser setups resolve localhost through a different loopback family.
      // Retry only the local alias; never silently change a configured remote API host.
      if (API_URL_FALLBACK !== API_URL && API_URL.includes("localhost")) {
        response = await request(API_URL_FALLBACK);
      } else {
        throw firstError;
      }
    }
    const raw = await response.text();
    try { data = raw ? JSON.parse(raw) : null; } catch { data = null; }
  } catch {
    throw new ApiError("No se pudo conectar con el servidor. Verificá que el backend esté encendido e intentá nuevamente.");
  } finally {
    clearTimeout(timeout);
  }

  if (!response.ok) {
    if (response.status === 401) {
      // An old request must never clear a newer login session.
      if (auth && token === readStorage(TOKEN_KEY, null)) {
        clearSession();
        if (typeof window !== "undefined") {
          window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT));
          if (!["/login", "/registro"].includes(window.location.pathname.replace(/\/$/, ""))) {
            window.location.replace("/login?sesion=expirada");
          }
        }
      }
      throw new ApiError(auth ? "Tu sesión venció. Volvé a iniciar sesión." : "Email o contraseña incorrectos.", 401);
    }
    if (response.status === 403) throw new ApiError("No tenés autorización para realizar esta acción.", 403);
    if (response.status >= 500) throw new ApiError("El servidor no pudo completar la acción. Intentá nuevamente.", response.status);
    const message = typeof data?.message === "string" ? data.message : "No se pudo completar la acción.";
    const friendly = /must |must not|no debe|:|Exception|java\./i.test(message)
      ? "Revisá los datos ingresados e intentá nuevamente." : message;
    throw new ApiError(friendly, response.status);
  }
  if (response.status !== 204 && data === null) {
    throw new ApiError("El servidor devolvió una respuesta inesperada. Intentá nuevamente.");
  }
  return data;
}
