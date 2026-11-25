import { test, expect } from "@playwright/test";

const PASSWORD = "123456";

async function login(page, email, password = PASSWORD) {
  await page.goto("/login");
  await page.fill("input[type=email]", email);
  await page.fill("input[type=password]", password);
  await page.click("button[type=submit]");
}

test("E2E-01 Login con contraseña incorrecta muestra un error y no ingresa", async ({ page }) => {
  await login(page, "cliente@panterfitness.com", "incorrecta");
  await expect(page.locator(".form-error")).toContainText(/incorrect/i);
  await expect(page).toHaveURL(/\/login/);
});

test("E2E-02 Cada rol ingresa a su propio panel", async ({ page }) => {
  for (const [email, ruta] of [
    ["cliente@panterfitness.com", /\/cliente$/],
    ["profesor@panterfitness.com", /\/profesor$/],
    ["admin@panterfitness.com", /\/admin$/]
  ]) {
    await page.context().clearCookies();
    await page.goto("/login");
    await page.evaluate(() => localStorage.clear());
    await login(page, email);
    await expect(page).toHaveURL(ruta);
  }
});

test("E2E-03 Rutas protegidas: sin sesión redirige al login y un cliente no entra al panel admin", async ({ page }) => {
  await page.goto("/admin");
  await expect(page).toHaveURL(/\/login/);
  await login(page, "cliente@panterfitness.com");
  await expect(page).toHaveURL(/\/cliente$/);
  await page.goto("/admin/usuarios");
  await expect(page).toHaveURL(/\/cliente$/);
});

test("E2E-04 Un cliente nuevo se registra, reserva una clase y la ve en Mis reservas", async ({ page }) => {
  const email = `e2e-${Date.now()}@example.test`;
  await page.goto("/registro");
  await page.locator("form input").nth(0).fill("Prueba");
  await page.locator("form input").nth(1).fill("E2E");
  await page.fill("input[type=email]", email);
  await page.locator("input[type=password]").first().fill(PASSWORD);
  const confirmar = page.locator("input[type=password]").nth(1);
  if (await confirmar.count()) await confirmar.fill(PASSWORD);
  await page.click("button[type=submit]");
  await expect(page).toHaveURL(/\/cliente$/);

  await page.goto("/cliente/clases");
  const reservar = page.getByRole("button", { name: "Reservar", exact: true }).first();
  await expect(reservar).toBeVisible();
  await reservar.click();
  await expect(page.getByText("Ya tenés lugar reservado").first()).toBeVisible();

  await page.goto("/cliente/reservas");
  await expect(page.getByText(/CONFIRMADA|Confirmada/).first()).toBeVisible();
});

test("E2E-05 Un cliente con membresía vencida no puede reservar y ve el motivo", async ({ page }) => {
  await login(page, "vencido@panterfitness.com");
  await expect(page).toHaveURL(/\/cliente$/);
  await page.goto("/cliente/clases");
  const reservar = page.getByRole("button", { name: "Reservar", exact: true }).first();
  await reservar.click();
  await expect(page.getByText(/membres[ií]a/i).first()).toBeVisible();
  await expect(page.getByText("Ya tenés lugar reservado")).toHaveCount(0);
});

// ---------------------------------------------------------------------------------------------
// E2E-06: ingreso real con QR. El cliente muestra su QR; recepción lo lee con la cámara.
// La cámara se emula con un video generado a partir de la imagen del QR del cliente
// (Chromium --use-file-for-fake-video-capture), así se prueba el lector de verdad.
// ---------------------------------------------------------------------------------------------
import { chromium } from "@playwright/test";
import { execFileSync } from "node:child_process";
import { mkdtempSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";

const GYM_TZ = "America/Argentina/Buenos_Aires";

function gymNow(offsetMinutes = 0) {
  const parts = Object.fromEntries(new Intl.DateTimeFormat("en-CA", {
    timeZone: GYM_TZ, year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit", hourCycle: "h23"
  }).formatToParts(new Date(Date.now() + offsetMinutes * 60000)).map((p) => [p.type, p.value]));
  return { fecha: `${parts.year}-${parts.month}-${parts.day}`, hora: `${parts.hour}:${parts.minute}` };
}

async function api(page, path, method = "GET", body) {
  return page.evaluate(async ({ path, method, body }) => {
    const token = JSON.parse(localStorage.getItem("pantera_jwt"));
    const res = await fetch(path, {
      method, headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
      body: body ? JSON.stringify(body) : undefined
    });
    const text = await res.text();
    return { status: res.status, json: text ? JSON.parse(text) : null };
  }, { path, method, body });
}

test("E2E-06 Recepción escanea con la cámara el QR real del cliente y se registra el ingreso", async ({ page }) => {
  test.setTimeout(90_000);
  const inicio = gymNow(5);
  const fin = gymNow(50);
  test.skip(inicio.fecha !== fin.fecha, "La clase de prueba cruzaría la medianoche");

  // 1) El administrador crea una clase que empieza en 5 minutos.
  await login(page, "admin@panterfitness.com");
  await expect(page).toHaveURL(/\/admin$/);
  const clases = await api(page, "/api/clases");
  const horario = await api(page, "/api/horarios", "POST", {
    claseGimnasioId: clases.json[0].id, fecha: inicio.fecha, horaInicio: inicio.hora, horaFin: fin.hora, cupoMaximo: 10, activa: true
  });
  expect(horario.status).toBe(201);

  // 2) Un cliente nuevo se registra y reserva esa clase.
  await page.evaluate(() => localStorage.clear());
  await page.goto("/registro");
  await page.locator("form input").nth(0).fill("Socia");
  await page.locator("form input").nth(1).fill("QR");
  await page.fill("input[type=email]", `qr-${Date.now()}@example.test`);
  await page.locator("input[type=password]").first().fill(PASSWORD);
  await page.click("button[type=submit]");
  await expect(page).toHaveURL(/\/cliente$/);
  const reserva = await api(page, "/api/reservas", "POST", { horarioClaseId: horario.json.id });
  expect(reserva.status).toBe(201);

  // 3) El cliente abre "Mi QR": es una imagen QR real con su código.
  await page.goto("/cliente/qr");
  const qrImg = page.getByRole("img", { name: /Código QR de ingreso/ });
  await expect(qrImg).toBeVisible();
  await expect(page.getByText("Pendiente de ingreso")).toBeVisible();
  const dataUrl = await qrImg.getAttribute("src");
  expect(dataUrl).toMatch(/^data:image\/png;base64,/);

  // 4) Convertimos la imagen en un video de cámara falsa y recepción la escanea.
  const dir = mkdtempSync(join(tmpdir(), "pantera-qr-"));
  const png = join(dir, "qr.png");
  const y4m = join(dir, "qr.y4m");
  writeFileSync(png, Buffer.from(dataUrl.split(",")[1], "base64"));
  execFileSync("ffmpeg", ["-loglevel", "error", "-y", "-loop", "1", "-i", png, "-vf", "scale=640:640,format=yuv420p", "-t", "2", "-r", "10", y4m]);
  const browser = await chromium.launch({
    args: ["--use-fake-ui-for-media-stream", "--use-fake-device-for-media-stream", `--use-file-for-fake-video-capture=${y4m}`]
  });
  try {
    const recepcion = await (await browser.newContext({
      baseURL: test.info().project.use.baseURL, permissions: ["camera"], viewport: { width: 1366, height: 800 }
    })).newPage();
    await login(recepcion, "admin@panterfitness.com");
    await expect(recepcion).toHaveURL(/\/admin$/);
    await recepcion.goto("/admin/ingreso");
    await recepcion.getByRole("button", { name: /Activar cámara/ }).click();
    await expect(recepcion.getByRole("status")).toContainText("Ingreso registrado: Socia QR", { timeout: 20_000 });
    await expect(recepcion.getByText("Últimos ingresos")).toBeVisible();
  } finally {
    await browser.close();
  }

  // 5) Sin tocar nada, la pantalla del cliente se actualiza y muestra el ingreso.
  await expect(page.getByText("Ingreso registrado").first()).toBeVisible({ timeout: 20_000 });
});

test("E2E-07 Recepción: un código inexistente se rechaza y un cliente no ve el lector", async ({ page }) => {
  await login(page, "admin@panterfitness.com");
  await expect(page).toHaveURL(/\/admin$/);
  await page.goto("/admin/ingreso");
  await page.fill("#codigo-qr", "PF-codigo-inventado");
  await page.getByRole("button", { name: "Registrar" }).click();
  await expect(page.getByRole("status")).toContainText("ningún socio");
  await page.evaluate(() => localStorage.clear());
  await login(page, "cliente@panterfitness.com");
  await expect(page).toHaveURL(/\/cliente$/);
  await page.goto("/admin/ingreso");
  await expect(page).toHaveURL(/\/cliente$/);
});
