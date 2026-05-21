export function isBrowser() {
  return typeof window !== "undefined";
}

export function readStorage(key, fallback) {
  if (!isBrowser()) {
    return fallback;
  }

  try {
    const rawValue = window.localStorage.getItem(key);
    return rawValue ? JSON.parse(rawValue) : fallback;
  } catch {
    return fallback;
  }
}

export function writeStorage(key, value) {
  if (!isBrowser()) {
    return;
  }

  window.localStorage.setItem(key, JSON.stringify(value));
}

export function removeStorage(key) {
  if (!isBrowser()) {
    return;
  }

  window.localStorage.removeItem(key);
}
