"use client";

import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { classService } from "@/services/classService";
import { notificationService } from "@/services/notificationService";
import { occupancyService } from "@/services/occupancyService";
import { reservationService } from "@/services/reservationService";
import { useAuth } from "@/context/AuthContext";

const AppDataContext = createContext(null);

function feedbackFromError(error) {
  return {
    id: Date.now(),
    tipo: "ERROR",
    mensaje: error.message || "No se pudo completar la accion."
  };
}

export function AppDataProvider({ children }) {
  const { user } = useAuth();
  const [classes, setClasses] = useState([]);
  const [reservations, setReservations] = useState([]);
  const [occupancy, setOccupancy] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const [feedback, setFeedback] = useState(null);

  useEffect(() => {
    const loadedClasses = classService.getWeeklyClasses();
    setClasses(loadedClasses);
    setReservations(reservationService.getReservations(loadedClasses));
    setOccupancy(occupancyService.getCurrentOccupancy());
  }, []);

  useEffect(() => {
    if (!occupancy) {
      return undefined;
    }

    const intervalId = window.setInterval(() => {
      setOccupancy((currentOccupancy) =>
        currentOccupancy
          ? occupancyService.simulateNextOccupancy(currentOccupancy)
          : currentOccupancy
      );
    }, 10000);

    return () => window.clearInterval(intervalId);
  }, [occupancy]);

  useEffect(() => {
    if (!user) {
      setNotifications([]);
      return;
    }

    setNotifications(notificationService.getByUser(user.id));
  }, [user]);

  function showFeedback(mensaje, tipo = "SUCCESS") {
    setFeedback({
      id: Date.now(),
      tipo,
      mensaje
    });
  }

  function createNotification(notification, targetUserId = user?.id) {
    if (!targetUserId) {
      return;
    }

    const allNotifications = notificationService.createNotification({
      userId: targetUserId,
      ...notification
    });

    if (user) {
      setNotifications(allNotifications.filter((item) => item.userId === user.id));
    }
  }

  function handleReservationError(error) {
    setFeedback(feedbackFromError(error));

    if (user && error.message.toLowerCase().includes("membresia")) {
      createNotification({
        titulo: "Membresia vencida",
        mensaje: error.message,
        tipo: "ERROR"
      });
    }
  }

  function reserveClass(classId) {
    const classItem = classes.find((item) => item.id === classId);

    if (!classItem) {
      showFeedback("No se encontro la clase seleccionada.", "ERROR");
      return;
    }

    try {
      const result = reservationService.createReservation({
        user,
        classItem,
        classes,
        reservations
      });
      setClasses(result.classes);
      setReservations(result.reservations);
      createNotification(result.notification);
      showFeedback(result.message, "SUCCESS");
    } catch (error) {
      handleReservationError(error);
    }
  }

  function joinWaitList(classId) {
    const classItem = classes.find((item) => item.id === classId);

    if (!classItem) {
      showFeedback("No se encontro la clase seleccionada.", "ERROR");
      return;
    }

    try {
      const result = reservationService.joinWaitList({
        user,
        classItem,
        classes,
        reservations
      });
      setClasses(result.classes);
      setReservations(result.reservations);
      createNotification(result.notification);
      showFeedback(result.message, "WARNING");
    } catch (error) {
      handleReservationError(error);
    }
  }

  function cancelReservation(reservationId) {
    try {
      const result = reservationService.cancelReservation({
        reservationId,
        classes,
        reservations
      });
      setClasses(result.classes);
      setReservations(result.reservations);
      createNotification(result.notification);

      if (result.promotedNotification) {
        createNotification(result.promotedNotification, result.promotedNotification.userId);
      }

      showFeedback(result.message, "INFO");
    } catch (error) {
      setFeedback(feedbackFromError(error));
    }
  }

  function simulateQr(reservationId, mode = "ASISTIDA") {
    try {
      const result =
        mode === "AUSENTE"
          ? reservationService.simulateLateQrCheckIn({ user, reservationId, reservations })
          : reservationService.simulateQrCheckIn({ user, reservationId, reservations });
      setReservations(result.reservations);
      createNotification(result.notification);
      showFeedback(result.message, mode === "AUSENTE" ? "WARNING" : "SUCCESS");
    } catch (error) {
      setFeedback(feedbackFromError(error));
    }
  }

  function markAllNotificationsAsRead() {
    if (!user) {
      return;
    }

    const allNotifications = notificationService.markAllAsRead(user.id);
    setNotifications(allNotifications.filter((item) => item.userId === user.id));
  }

  const value = useMemo(
    () => ({
      classes,
      reservations,
      occupancy,
      notifications,
      feedback,
      setFeedback,
      reserveClass,
      joinWaitList,
      cancelReservation,
      simulateQr,
      markAllNotificationsAsRead
    }),
    [classes, reservations, occupancy, notifications, feedback, user]
  );

  return <AppDataContext.Provider value={value}>{children}</AppDataContext.Provider>;
}

export function useAppData() {
  const context = useContext(AppDataContext);

  if (!context) {
    throw new Error("useAppData debe usarse dentro de AppDataProvider.");
  }

  return context;
}
