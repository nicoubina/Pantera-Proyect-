package com.sistema.panterafitness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication
public class PanterfitnessApplication {

  /** Zona horaria del gimnasio: las reglas de reserva comparan horarios locales (LocalDateTime). */
  static final String ZONA_GIMNASIO = "America/Argentina/Buenos_Aires";

  public static void main(String[] args) {
    // Los servidores de hosting corren en UTC: sin esto una clase de las 18:00 se evaluaria 3 h corrida.
    String zona = System.getenv().getOrDefault("APP_TIMEZONE", ZONA_GIMNASIO);
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(zona));
    SpringApplication.run(PanterfitnessApplication.class, args);
  }
}
