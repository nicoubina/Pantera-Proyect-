"use client";

import { createContext, useCallback, useContext, useEffect, useRef, useState } from "react";
import { classService } from "@/services/classService";
import { notificationService } from "@/services/notificationService";
import { occupancyService } from "@/services/occupancyService";
import { reservationService } from "@/services/reservationService";
import { qrService } from "@/services/qrService";
import { userService } from "@/services/userService";
import { useAuth } from "@/context/AuthContext";
import { modulesService } from "@/services/modulesService";

const AppDataContext = createContext(null);
const emptyData = { classes: [], reservations: [], occupancy: null, notifications: [], users: [], catalog: [], alerts: [] };

export function AppDataProvider({ children }) {
  const { user } = useAuth();
  const [data, setData] = useState(emptyData);
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState("");
  const [feedback, setFeedback] = useState(null);
  const [pending, setPending] = useState(false);
  const actionLock = useRef(false);
  const generation = useRef(0);
  const requestVersion = useRef(0);
  const owner = useRef(null);

  const refresh = useCallback(async () => {
    if (!user) return;
    const session = generation.current;
    const version = ++requestVersion.current;
    try {
      const [occupancy, classOccupancies, reservations, notifications, catalog, users, alerts] = await Promise.all([
        occupancyService.getCurrentOccupancy(), occupancyService.getClassesOccupancy(),
        reservationService.getReservations(user.rol), notificationService.getByUser(),
        classService.getClasses(),
        user.rol === "ADMINISTRADOR" ? userService.getAllUsers() : Promise.resolve([]),
        modulesService.list("alertas")
      ]);
      if (session !== generation.current || version !== requestVersion.current) return;
      const classes = await classService.getWeeklyClasses(classOccupancies);
      if (session !== generation.current || version !== requestVersion.current) return;
      setData({ classes, reservations, occupancy, notifications, catalog, users, alerts });
      setLoadError("");
    } catch (error) {
      if (session === generation.current && version === requestVersion.current) setLoadError(error.message);
    } finally {
      if (session === generation.current && version === requestVersion.current) setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    generation.current += 1;
    owner.current = user?.id;
    setData(emptyData);
    setLoadError("");
    setFeedback(null);
    setPending(false);
    actionLock.current = false;
    setLoading(Boolean(user));
    if (!user) return;
    let stopped = false;
    let timer;
    async function poll() {
      if (!actionLock.current) await refresh();
      if (!stopped) timer = window.setTimeout(poll, 10000);
    }
    poll();
    return () => {
      stopped = true;
      generation.current += 1;
      window.clearTimeout(timer);
    };
  }, [user, refresh]);

  function showFeedback(mensaje, tipo = "SUCCESS") {
    setFeedback({ id: Date.now(), tipo, mensaje });
  }

  async function runAction(action, successMessage) {
    if (!user || actionLock.current) return null;
    const session = generation.current;
    actionLock.current = true;
    setPending(true);
    // Ignore polling responses started before the mutation.
    requestVersion.current += 1;
    let result = null;
    try {
      result = await action();
      if (session !== generation.current) return null;
      showFeedback(typeof successMessage === "function" ? successMessage(result) : successMessage);
    } catch (error) {
      if (session === generation.current) showFeedback(error.message, "ERROR");
    } finally {
      if (session === generation.current) {
        // Refresh even after a failure: the server may have persisted a notification.
        await refresh();
        actionLock.current = false;
        setPending(false);
      }
    }
    return result;
  }

  function reserveClass(classId) {
    return runAction(() => reservationService.createReservation(classId),
      (result) => result.estado === "EN_ESPERA"
        ? `Clase llena. Ingresaste a la lista de espera en posición ${result.posicionListaEspera}.`
        : "Reserva confirmada.");
  }

  function simulateQr(reservationId, mode = "ASISTIDA") {
    const reservation = data.reservations.find((item) => item.id === reservationId);
    if (!reservation) { showFeedback("Seleccioná una reserva confirmada.", "ERROR"); return Promise.resolve(null); }
    const schedule = reservation.classItem;
    // Both buttons explicitly simulate a local class time; the API determines attendance.
    const time = new Date(`${schedule.fecha}T${schedule.hora}:00`);
    if (mode === "AUSENTE") time.setMinutes(time.getMinutes() + 11);
    const pad = (value) => String(value).padStart(2, "0");
    const simulatedTime = `${time.getFullYear()}-${pad(time.getMonth() + 1)}-${pad(time.getDate())}T${pad(time.getHours())}:${pad(time.getMinutes())}:00`;
    return runAction(() => qrService.simulateCheckIn({
      qrSimulado: user.qrSimulado, horarioClaseId: reservation.classId, horaIngresoSimulada: simulatedTime
    }), (result) => result.mensaje);
  }

  // Do not expose the previous account's data during a role/session switch.
  const visible = owner.current === user?.id ? data : emptyData;
  const value = {
    ...visible, loading, loadError, pending, feedback, setFeedback, refresh,
    reserveClass, joinWaitList: reserveClass,
    cancelReservation: (id) => runAction(() => reservationService.cancelReservation(id), (result) => result.mensaje),
    simulateQr,
    markAllNotificationsAsRead: () => runAction(async () => {
      await notificationService.markAllAsRead(data.notifications);
      return true;
    }, "Notificaciones marcadas como leídas."),
    markNotificationAsRead: (id) => runAction(() => notificationService.markAsRead(id), "Notificación marcada como leída."),
    updateMembership: (id, membership) => runAction(() => userService.updateMembership(id, membership), "Membresía actualizada.")
  };
  return <AppDataContext.Provider value={value}>{children}</AppDataContext.Provider>;
}

export function useAppData() {
  const context = useContext(AppDataContext);
  if (!context) throw new Error("useAppData debe usarse dentro de AppDataProvider.");
  return context;
}
