package com.sistema.panterafitness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication
public class PanterfitnessApplication {

  public static void main(String[] args) {
    SpringApplication.run(PanterfitnessApplication.class, args);
  }
}
