package com.sistema.panterafitness.entity;

import com.sistema.panterafitness.enums.MetodoRegistro;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Guarda {@link MetodoRegistro#QR} con el valor historico "QR_SIMULADO" para no romper el CHECK de
 * la base ya migrada. El resto de los valores se guarda con su nombre.
 */
@Converter
public class MetodoRegistroConverter implements AttributeConverter<MetodoRegistro, String> {

  static final String VALOR_QR_EN_BASE = "QR_SIMULADO";

  @Override
  public String convertToDatabaseColumn(MetodoRegistro metodo) {
    if (metodo == null) return null;
    return metodo == MetodoRegistro.QR ? VALOR_QR_EN_BASE : metodo.name();
  }

  @Override
  public MetodoRegistro convertToEntityAttribute(String valor) {
    if (valor == null) return null;
    return VALOR_QR_EN_BASE.equals(valor) || "QR".equals(valor)
        ? MetodoRegistro.QR
        : MetodoRegistro.valueOf(valor);
  }
}
