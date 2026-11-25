// Pruebas E2E (de punta a punta) sobre la aplicacion en ejecucion.
// Requiere el sistema encendido con datos demo, por ejemplo el JAR unificado con perfil H2:
//   java -jar pantera-fitness.jar --spring.profiles.active=h2      (http://localhost:8080)
// o backend + "npm run dev" y E2E_BASE_URL=http://localhost:3000.
// Primera vez: npx playwright install chromium
import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./e2e",
  timeout: 30_000,
  retries: 0,
  workers: 1,
  reporter: [["list"]],
  use: {
    baseURL: process.env.E2E_BASE_URL || "http://localhost:8080",
    viewport: { width: 1366, height: 800 },
    trace: "off",
    screenshot: "only-on-failure"
  }
});
