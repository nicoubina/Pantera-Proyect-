package com.sistema.panterafitness.enums;

public enum MetodoRegistro {
  /** Ingreso registrado al escanear el QR personal del cliente. */
  QR,
  /** Asistencia cargada a mano por el profesor o el administrador. */
  MANUAL,
  /** Cierre automatico: reserva sin ingreso marcada como ausente. */
  AUTOMATICO
}
