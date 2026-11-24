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
