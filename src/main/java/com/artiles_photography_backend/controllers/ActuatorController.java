package com.artiles_photography_backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/actuator")
@PreAuthorize("hasRole('ADMIN')")
public class ActuatorController {

  @Autowired
  private HealthEndpoint healthEndpoint;

  @Autowired
  private InfoEndpoint infoEndpoint;

  @Autowired
  private MetricsEndpoint metricsEndpoint;

  @GetMapping
  public ResponseEntity<?> getActuatorData() {
    try {
      // Obtener datos directamente de los endpoints de Actuator
      Object health = healthEndpoint.health();
      Object info = infoEndpoint.info();
      Object metrics = metricsEndpoint.listNames();

      // Combinar respuestas
      ActuatorResponse response = new ActuatorResponse(health, info, metrics);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(new ErrorResponse("Error fetching actuator data: " + e.getMessage()));
    }
  }

  // Clase para estructurar la respuesta
  public static class ActuatorResponse {
    public Object health;
    public Object info;
    public Object metrics;

    public ActuatorResponse(Object health, Object info, Object metrics) {
      this.health = health;
      this.info = info;
      this.metrics = metrics;
    }
  }

  // Clase para estructurar errores
  public static class ErrorResponse {
    public String error;

    public ErrorResponse(String error) {
      this.error = error;
    }
  }
}