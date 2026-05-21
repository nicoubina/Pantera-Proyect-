import { MEMBRESIAS, ROLES } from "@/data/mockUsers";
import {
  classService,
  getClassDateTime,
  getClassEndDateTime
} from "@/services/classService";
import { readStorage, writeStorage } from "@/services/storageService";

export const RESERVA_ESTADOS = {
  CONFIRMADA: "CONFIRMADA",
  CANCELADA: "CANCELADA",
  EN_ESPERA: "EN_ESPERA",
  ASISTIDA: "ASISTIDA",
  AUSENTE: "AUSENTE"
};

const RESERVATIONS_KEY = "pantera_reservations";
const ACTIVE_STATES = [
  RESERVA_ESTADOS.CONFIRMADA,
  RESERVA_ESTADOS.EN_ESPERA,
  RESERVA_ESTADOS.ASISTIDA,
  RESERVA_ESTADOS.AUSENTE
];

function buildSeedReservations(classes) {
  if (!classes.length) {
    return [];
  }

  const firstClass = classes[0];
  const secondClass = classes[1] || classes[0];
  const fullClass = classes.find((classItem) => classItem.cuposOcupados >= classItem.cupoTotal);

  return [
    {
      id: "reserva-demo-001",
      userId: "cliente-demo-ocupado",
      userName: "Cliente Demo",
      classId: firstClass.id,
      estado: RESERVA_ESTADOS.CONFIRMADA,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    },
    {
      id: "reserva-demo-002",
      userId: "cliente-demo-asistencia",
      userName: "Cliente Asistencia",
      classId: secondClass.id,
      estado: RESERVA_ESTADOS.ASISTIDA,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    },
    ...(fullClass
      ? [
          {
            id: "reserva-demo-espera-001",
            userId: "cliente-demo-espera",
            userName: "Cliente en Espera",
            classId: fullClass.id,
            estado: RESERVA_ESTADOS.EN_ESPERA,
            createdAt: new Date(Date.now() - 60 * 60 * 1000).toISOString(),
            updatedAt: new Date(Date.now() - 60 * 60 * 1000).toISOString()
          }
        ]
      : [])
  ];
}

function saveReservations(reservations) {
  writeStorage(RESERVATIONS_KEY, reservations);
}

function getClassById(classes, classId) {
  return classes.find((classItem) => classItem.id === classId);
}

function getMillisecondsUntilStart(classItem) {
  return getClassDateTime(classItem).getTime() - Date.now();
}

function assertClienteActivo(user) {
  if (!user || user.rol !== ROLES.CLIENTE) {
    throw new Error("Solo un cliente puede realizar reservas en este MVP.");
  }

  if (user.membresia !== MEMBRESIAS.ACTIVA) {
    throw new Error("Tu membresia esta vencida. Regularizala para poder reservar.");
  }
}

function assertReservationWindow(classItem) {
  const millisecondsUntilStart = getMillisecondsUntilStart(classItem);
  const oneWeek = 7 * 24 * 60 * 60 * 1000;
  const thirtyMinutes = 30 * 60 * 1000;

  if (millisecondsUntilStart > oneWeek) {
    throw new Error("La reserva puede realizarse como maximo con 1 semana de anticipacion.");
  }

  if (millisecondsUntilStart < thirtyMinutes) {
    throw new Error("La reserva debe realizarse al menos 30 minutos antes del inicio.");
  }
}

function assertNoDuplicate(user, classItem, reservations) {
  const duplicated = reservations.some(
    (reservation) =>
      reservation.userId === user.id &&
      reservation.classId === classItem.id &&
      ACTIVE_STATES.includes(reservation.estado)
  );

  if (duplicated) {
    throw new Error("Ya existe una reserva o lista de espera para esa clase.");
  }
}

function classesOverlap(firstClass, secondClass) {
  const firstStart = getClassDateTime(firstClass);
  const firstEnd = getClassEndDateTime(firstClass);
  const secondStart = getClassDateTime(secondClass);
  const secondEnd = getClassEndDateTime(secondClass);
  return firstStart < secondEnd && secondStart < firstEnd;
}

function assertNoScheduleOverlap(user, classItem, reservations, classes) {
  const overlappingReservation = reservations.find((reservation) => {
    if (
      reservation.userId !== user.id ||
      reservation.estado !== RESERVA_ESTADOS.CONFIRMADA ||
      reservation.classId === classItem.id
    ) {
      return false;
    }

    const reservedClass = getClassById(classes, reservation.classId);
    return reservedClass ? classesOverlap(reservedClass, classItem) : false;
  });

  if (overlappingReservation) {
    throw new Error("Tenes otra clase confirmada superpuesta en ese horario.");
  }
}

function updateClassOccupancy(classes, classId, delta) {
  return classes.map((classItem) => {
    if (classItem.id !== classId) {
      return classItem;
    }

    return {
      ...classItem,
      cuposOcupados: Math.min(
        Math.max(classItem.cuposOcupados + delta, 0),
        classItem.cupoTotal
      )
    };
  });
}

function promoteFirstFromWaitList({ reservations, classes, classId }) {
  const targetClass = getClassById(classes, classId);
  const hasAvailableSeat = targetClass && targetClass.cuposOcupados < targetClass.cupoTotal;

  if (!hasAvailableSeat) {
    return { reservations, classes, promotedReservation: null };
  }

  const firstWaiting = reservations
    .filter(
      (reservation) =>
        reservation.classId === classId && reservation.estado === RESERVA_ESTADOS.EN_ESPERA
    )
    .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))[0];

  if (!firstWaiting) {
    return { reservations, classes, promotedReservation: null };
  }

  const nextReservations = reservations.map((reservation) =>
    reservation.id === firstWaiting.id
      ? {
          ...reservation,
          estado: RESERVA_ESTADOS.CONFIRMADA,
          updatedAt: new Date().toISOString()
        }
      : reservation
  );
  const nextClasses = updateClassOccupancy(classes, classId, 1);

  return {
    reservations: nextReservations,
    classes: nextClasses,
    promotedReservation: {
      ...firstWaiting,
      estado: RESERVA_ESTADOS.CONFIRMADA
    }
  };
}

export function isReservationCancelable(reservation, classItem) {
  if (!reservation || !classItem || reservation.estado === RESERVA_ESTADOS.CANCELADA) {
    return false;
  }

  if (reservation.estado === RESERVA_ESTADOS.EN_ESPERA) {
    return true;
  }

  if (reservation.estado !== RESERVA_ESTADOS.CONFIRMADA) {
    return false;
  }

  const twentyFourHours = 24 * 60 * 60 * 1000;
  return getMillisecondsUntilStart(classItem) >= twentyFourHours;
}

export const reservationService = {
  getReservations(classes = []) {
    const stored = readStorage(RESERVATIONS_KEY, null);

    if (Array.isArray(stored)) {
      return stored;
    }

    const seedReservations = buildSeedReservations(classes);
    saveReservations(seedReservations);
    return seedReservations;
  },

  createReservation({ user, classItem, classes, reservations }) {
    assertClienteActivo(user);
    assertReservationWindow(classItem);
    assertNoDuplicate(user, classItem, reservations);
    assertNoScheduleOverlap(user, classItem, reservations, classes);

    if (classItem.cuposOcupados >= classItem.cupoTotal) {
      throw new Error("La clase esta llena. Podes ingresar a lista de espera.");
    }

    const newReservation = {
      id: `reserva-${Date.now()}-${Math.random().toString(16).slice(2)}`,
      userId: user.id,
      userName: user.nombre,
      classId: classItem.id,
      estado: RESERVA_ESTADOS.CONFIRMADA,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    const nextReservations = [newReservation, ...reservations];
    const nextClasses = updateClassOccupancy(classes, classItem.id, 1);

    saveReservations(nextReservations);
    classService.saveWeeklyClasses(nextClasses);

    return {
      reservations: nextReservations,
      classes: nextClasses,
      message: "Reserva confirmada.",
      notification: {
        titulo: "Reserva confirmada",
        mensaje: `${classItem.nombre} quedo confirmada para ${classItem.diaNombre} ${classItem.hora}.`,
        tipo: "SUCCESS"
      }
    };
  },

  joinWaitList({ user, classItem, classes, reservations }) {
    assertClienteActivo(user);
    assertReservationWindow(classItem);
    assertNoDuplicate(user, classItem, reservations);

    if (classItem.cuposOcupados < classItem.cupoTotal) {
      throw new Error("La clase tiene cupos disponibles. Podes reservar directamente.");
    }

    const newReservation = {
      id: `espera-${Date.now()}-${Math.random().toString(16).slice(2)}`,
      userId: user.id,
      userName: user.nombre,
      classId: classItem.id,
      estado: RESERVA_ESTADOS.EN_ESPERA,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    const nextReservations = [newReservation, ...reservations];

    saveReservations(nextReservations);

    return {
      reservations: nextReservations,
      classes,
      message: "Clase llena, ingresaste a lista de espera.",
      notification: {
        titulo: "Ingreso a lista de espera",
        mensaje: `${classItem.nombre} esta completa. Te avisaremos si se libera un cupo.`,
        tipo: "WARNING"
      }
    };
  },

  cancelReservation({ reservationId, classes, reservations }) {
    const reservation = reservations.find((item) => item.id === reservationId);

    if (!reservation) {
      throw new Error("No se encontro la reserva.");
    }

    const classItem = getClassById(classes, reservation.classId);

    if (!isReservationCancelable(reservation, classItem)) {
      throw new Error("No se puede cancelar por estar dentro de las 24 horas previas.");
    }

    let nextClasses = classes;
    let nextReservations = reservations.map((item) =>
      item.id === reservationId
        ? {
            ...item,
            estado: RESERVA_ESTADOS.CANCELADA,
            updatedAt: new Date().toISOString()
          }
        : item
    );

    if (reservation.estado === RESERVA_ESTADOS.CONFIRMADA) {
      nextClasses = updateClassOccupancy(nextClasses, reservation.classId, -1);
      const promoted = promoteFirstFromWaitList({
        reservations: nextReservations,
        classes: nextClasses,
        classId: reservation.classId
      });
      nextReservations = promoted.reservations;
      nextClasses = promoted.classes;

      saveReservations(nextReservations);
      classService.saveWeeklyClasses(nextClasses);

      return {
        reservations: nextReservations,
        classes: nextClasses,
        promotedReservation: promoted.promotedReservation,
        message: "Cancelacion realizada.",
        notification: {
          titulo: "Cancelacion realizada",
          mensaje: `${classItem.nombre} fue cancelada correctamente.`,
          tipo: "INFO"
        },
        promotedNotification: promoted.promotedReservation
          ? {
              userId: promoted.promotedReservation.userId,
              titulo: "Cupo asignado",
              mensaje: `Se libero un cupo y tu reserva para ${classItem.nombre} fue confirmada.`,
              tipo: "SUCCESS"
            }
          : null
      };
    }

    saveReservations(nextReservations);

    return {
      reservations: nextReservations,
      classes: nextClasses,
      promotedReservation: null,
      message: "Cancelacion realizada.",
      notification: {
        titulo: "Cancelacion realizada",
        mensaje: `${classItem.nombre} fue cancelada correctamente.`,
        tipo: "INFO"
      },
      promotedNotification: null
    };
  },

  simulateQrCheckIn({ user, reservationId, reservations }) {
    if (!user || user.rol !== ROLES.CLIENTE) {
      throw new Error("El check-in simulado esta disponible para clientes.");
    }

    const reservation = reservations.find(
      (item) =>
        item.id === reservationId &&
        item.userId === user.id &&
        item.estado === RESERVA_ESTADOS.CONFIRMADA
    );

    if (!reservation) {
      throw new Error("No tenes una reserva confirmada para este horario.");
    }

    const nextStatus = RESERVA_ESTADOS.ASISTIDA;
    const nextReservations = reservations.map((item) =>
      item.id === reservation.id
        ? {
            ...item,
            estado: nextStatus,
            updatedAt: new Date().toISOString()
          }
        : item
    );

    saveReservations(nextReservations);

    return {
      reservations: nextReservations,
      message: "QR simulado usado. Ingreso registrado como asistido.",
      notification: {
        titulo: "QR simulado usado",
        mensaje: "El ingreso fue registrado como asistencia simulada.",
        tipo: "SUCCESS"
      }
    };
  },

  simulateLateQrCheckIn({ user, reservationId, reservations }) {
    if (!user || user.rol !== ROLES.CLIENTE) {
      throw new Error("El check-in simulado esta disponible para clientes.");
    }

    const reservation = reservations.find(
      (item) =>
        item.id === reservationId &&
        item.userId === user.id &&
        item.estado === RESERVA_ESTADOS.CONFIRMADA
    );

    if (!reservation) {
      throw new Error("No tenes una reserva confirmada para este horario.");
    }

    const nextReservations = reservations.map((item) =>
      item.id === reservation.id
        ? {
            ...item,
            estado: RESERVA_ESTADOS.AUSENTE,
            updatedAt: new Date().toISOString()
          }
        : item
    );

    saveReservations(nextReservations);

    return {
      reservations: nextReservations,
      message: "QR simulado usado con llegada tarde. Reserva marcada como ausente.",
      notification: {
        titulo: "QR simulado usado",
        mensaje: "La llegada tarde simulada marco la reserva como ausente.",
        tipo: "WARNING"
      }
    };
  }
};
