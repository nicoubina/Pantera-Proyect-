// Run against the local demo backend. Creates a uniquely named client in H2.
import assert from "node:assert/strict";
import { registerHooks } from "node:module";

registerHooks({
  resolve(specifier, context, nextResolve) {
    return nextResolve(specifier.startsWith("./") && !specifier.endsWith(".js")
      ? specifier + ".js" : specifier, context);
  }
});
const storage = new Map();
let redirected = null;
globalThis.window = {
  localStorage: {
    getItem: (key) => storage.get(key) ?? null,
    setItem: (key, value) => storage.set(key, value),
    removeItem: (key) => storage.delete(key)
  },
  location: { pathname: "/cliente", replace: (path) => { redirected = path; } },
  dispatchEvent() {}
};

const { authService } = await import("../src/services/authService.js");
const { classService } = await import("../src/services/classService.js");
const { occupancyService } = await import("../src/services/occupancyService.js");
const { reservationService } = await import("../src/services/reservationService.js");
const { notificationService } = await import("../src/services/notificationService.js");
const { userService } = await import("../src/services/userService.js");
const { qrService } = await import("../src/services/qrService.js");
const { apiRequest, TOKEN_KEY, SESSION_KEY } = await import("../src/services/apiClient.js");

for (const [account, role] of [
  ["cliente", "CLIENTE"], ["vencido", "CLIENTE"], ["profesor", "PROFESOR"], ["admin", "ADMINISTRADOR"]
]) {
  const user = await authService.login(account + "@panterfitness.com", "123456");
  assert.equal(user.rol, role);
  assert.equal((await authService.getProfile()).id, user.id);
  assert.equal(authService.getCurrentUser().id, user.id);
  const schedules = await classService.getWeeklySchedules();
  assert.ok(schedules.length);
  const reservations = await reservationService.getReservations(role);
  if (role === "PROFESOR") {
    assert.ok(schedules.every((item) => item.claseGimnasio.profesor.id === user.id));
    assert.ok(reservations.every((item) => item.classItem.profesorId === user.id));
  }
  if (account === "vencido") {
    await assert.rejects(() => reservationService.createReservation(schedules[0].id), /membresia/i);
  }
  console.log("PASS login/profile/role: " + account);
}
const demoUser = await authService.register({
  nombre: "Prueba", apellido: "Integración", email: "integracion-" + Date.now() + "@example.test", password: "123456"
});
assert.equal(demoUser.rol, "CLIENTE");
assert.ok(demoUser.qrSimulado);
await assert.rejects(() => userService.getAllUsers(), (error) => error.status === 403);
assert.ok(authService.hasSession(), "403 must not clear the client session");

const catalog = await classService.getClasses();
assert.ok(catalog.length);
assert.ok((await classService.getSchedules()).length);
const occupation = await occupancyService.getClassesOccupancy();
const classes = await classService.getWeeklyClasses(occupation);
assert.ok(classes.every((item) => item.profesor && item.fecha && item.hora && item.duracionMinutos > 0));
const available = classes.filter((item) => item.cuposOcupados < item.cupoTotal &&
  new Date(item.fecha + "T" + item.hora).getTime() > Date.now() + 25 * 3600000 &&
  new Date(item.fecha + "T" + item.hora).getTime() < Date.now() + 7 * 86400000);
assert.ok(available.length >= 2, "The demo needs two future available schedules");
const full = classes.find((item) => item.cuposOcupados >= item.cupoTotal &&
  new Date(item.fecha + "T" + item.hora).getTime() > Date.now() + 25 * 3600000);
assert.ok(full, "Run with the seeded full class more than 24 hours away");

let reservation = await reservationService.createReservation(available[0].id);
assert.equal(reservation.estado, "CONFIRMADA");
assert.equal((await occupancyService.getClassOccupancy(available[0].id)).ocupacionActual, available[0].cuposOcupados + 1);
await assert.rejects(() => reservationService.createReservation(available[0].id), /ya tiene/i);
assert.ok((await reservationService.getReservations("CLIENTE")).some((item) => item.id === reservation.id));
assert.equal((await reservationService.cancelReservation(reservation.id)).reserva.estadoReserva, "CANCELADA");
assert.equal((await occupancyService.getClassOccupancy(available[0].id)).ocupacionActual, available[0].cuposOcupados);
const waiting = await reservationService.createReservation(full.id);
assert.equal(waiting.estado, "EN_ESPERA");
assert.ok(waiting.posicionListaEspera >= 2);
await reservationService.cancelReservation(waiting.id);
console.log("PASS reserve, duplicate, cancellation, occupancy and backend waitlist position");

const beforeQr = await occupancyService.getCurrentOccupancy();
reservation = await reservationService.createReservation(available[0].id);
const qr = await qrService.simulateCheckIn({
  qrSimulado: demoUser.qrSimulado, horarioClaseId: reservation.classId,
  horaIngresoSimulada: available[0].fecha + "T" + available[0].hora + ":00"
});
assert.equal(qr.estadoAsistencia, "ASISTIDA");
assert.equal((await occupancyService.getCurrentOccupancy()).total.personas, beforeQr.total.personas + 1);
await assert.rejects(() => qrService.simulateCheckIn({
  qrSimulado: demoUser.qrSimulado, horarioClaseId: reservation.classId
}), /CONFIRMADA/i);
const second = available[1];
await reservationService.createReservation(second.id);
const late = new Date(second.fecha + "T" + second.hora + ":00");
late.setMinutes(late.getMinutes() + 11);
const pad = (n) => String(n).padStart(2, "0");
const lateResult = await qrService.simulateCheckIn({
  qrSimulado: demoUser.qrSimulado, horarioClaseId: second.id,
  horaIngresoSimulada: late.getFullYear() + "-" + pad(late.getMonth() + 1) + "-" + pad(late.getDate()) +
    "T" + pad(late.getHours()) + ":" + pad(late.getMinutes()) + ":00"
});
assert.equal(lateResult.estadoAsistencia, "AUSENTE");
const notifications = await notificationService.getByUser();
assert.ok(notifications.some((item) => item.tipo === "QR"));
await notificationService.markAllAsRead(notifications);
assert.ok((await notificationService.getByUser()).every((item) => item.leida));
console.log("PASS QR attendance/late/rejection and notification persistence");

await authService.login("admin@panterfitness.com", "123456");
assert.ok((await userService.getAllUsers()).some((item) => item.id === demoUser.id));
const updated = await userService.updateMembership(demoUser.id, {
  estadoMembresia: "VENCIDA", fechaInicioMembresia: demoUser.fechaInicioMembresia,
  fechaVencimientoMembresia: demoUser.fechaVencimientoMembresia
});
assert.equal(updated.membresia, "VENCIDA");
storage.set(TOKEN_KEY, JSON.stringify("invalid-token"));
await assert.rejects(() => apiRequest("/api/usuarios/me"), (error) => error.status === 401);
assert.equal(storage.has(TOKEN_KEY), false);
assert.equal(storage.has(SESSION_KEY), false);
assert.equal(redirected, "/login?sesion=expirada");
await assert.rejects(() => authService.login("cliente@panterfitness.com", "wrong"), /incorrectos/);
assert.equal(authService.getCurrentUser(), null);
console.log("PASS admin membership, JWT cleanup and invalid login");
console.log("All real frontend service integration checks passed.");

const { modulesService } = await import("../src/services/modulesService.js");
await authService.login("admin@panterfitness.com", "123456");
const unique = Date.now();
const newClient = await modulesService.save("usuarios", null, {
  nombre: "Manual", apellido: "Integración", email: `modules-${unique}@example.test`,
  password: "123456", rol: "CLIENTE", activo: true
});
const dayKey = (date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
const today = dayKey(new Date());
const futureDate = dayKey(new Date(Date.now() + 3 * 86400000));
await modulesService.membership(newClient.id, { estadoMembresia: "ACTIVA", fechaInicioMembresia: today, fechaVencimientoMembresia: futureDate });
const professor = (await modulesService.list("usuarios")).find(u => u.email === "profesor@panterfitness.com");
const createdClass = await modulesService.save("clases", null, { nombre: `Integración ${unique}`, descripcion: "Clase de prueba", profesorId: professor.id, sector: "SALA_CLASES", cupoMaximo: 2, activa: true });
const createdSchedule = await modulesService.save("horarios", null, { claseGimnasioId: createdClass.id, fecha: futureDate, horaInicio: "10:00", horaFin: "11:00", cupoMaximo: 2, activa: true });
await modulesService.save("horarios", createdSchedule.id, { claseGimnasioId: createdClass.id, fecha: futureDate, horaInicio: "10:00", horaFin: "11:00", cupoMaximo: 3, activa: true });
const alert = await modulesService.save("alertas", null, { titulo: `Aviso ${unique}`, descripcion: "Aviso interno", activa: true, prioridad: "ALTA" });
await authService.login(newClient.email, "123456");
assert.equal((await modulesService.list("membresias/mi-membresia")).estado, "ACTIVA");
assert.ok((await modulesService.list("alertas")).some(a => a.id === alert.id));
await reservationService.createReservation(createdSchedule.id);
assert.ok((await modulesService.list("asistencias")).some(a => a.estadoAsistencia === "PENDIENTE"));
const clientNotifications = await modulesService.list("notificaciones/mis-notificaciones");
await authService.login(professor.email, "123456");
const exercise = await modulesService.save("ejercicios", null, { nombre: `Ejercicio ${unique}`, descripcion: "Control de técnica" });
const routineBody = { nombre: `Rutina ${unique}`, descripcion: "Plan de prueba", clienteId: newClient.id, estado: "ACTIVA", ejercicios: [{ ejercicioId: exercise.id, series: 3, repeticiones: 10, pesoSugerido: 5, descanso: 60 }] };
const routine = await modulesService.save("rutinas", null, routineBody);
const edited = await modulesService.save("rutinas", routine.id, { ...routineBody, estado: "PAUSADA", ejercicios: [{ ...routineBody.ejercicios[0], series: 4 }] });
assert.equal(edited.estado, "PAUSADA"); assert.equal(edited.ejercicios[0].series, 4);
await authService.login(newClient.email, "123456");
assert.equal((await modulesService.get("rutinas", routine.id)).ejercicios[0].ejercicio.nombre, exercise.nombre);
await authService.login("vencido@panterfitness.com", "123456");
await assert.rejects(() => modulesService.get("rutinas", routine.id), e => e.status === 403);
await authService.login("admin@panterfitness.com", "123456");
const penalty = await modulesService.save("penalizaciones", null, { usuarioId: newClient.id, motivo: "Revisión administrativa", fechaInicio: today, fechaFin: futureDate });
assert.equal((await modulesService.cancelPenalty(penalty.id)).estado, "CANCELADA");
await assert.rejects(() => apiRequest(`/api/notificaciones/${clientNotifications[0].id}/leer`, { method: "PATCH" }), e => e.status === 403);
await modulesService.save("alertas", alert.id, { ...alert, activa: false });
await modulesService.save("usuarios", newClient.id, { nombre: "Manual", apellido: "Actualizado", email: newClient.email, rol: "CLIENTE", activo: false });
await assert.rejects(() => authService.login(newClient.email, "123456"), e => e.status === 401);
await authService.login("admin@panterfitness.com", "123456");
assert.ok((await modulesService.list("estadisticas")).clasesPopulares.length);
console.log("PASS all new modules: users, memberships, classes, schedules, routine create/edit/ownership, alerts, penalties, statistics and notification ownership");
