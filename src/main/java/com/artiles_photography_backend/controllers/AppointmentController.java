/*
 * The MIT License
 *
 * Copyright 2025 arojas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.artiles_photography_backend.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.models.Appointment;
import com.artiles_photography_backend.services.AppointmentService;

import jakarta.validation.Valid;

/**
 * @author arojas
 *         Controlador REST para la gestión de citas.
 */
@RestController
@RequestMapping("/api")
@Validated
public class AppointmentController {

	private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);
	private final AppointmentService appointmentService;

	public AppointmentController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	@PostMapping("/appointments")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> createAppointment(@Valid @RequestBody Appointment appointment,
			@RequestParam String email) {
		logger.info("Creando cita para el cliente: {}, email: {}", appointment.getClientName(), email);
		try {
			if (email == null || email.trim().isEmpty()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "El correo electrónico es obligatorio"));
			}
			Appointment createdAppointment = appointmentService.createAppointment(appointment, email);
			return ResponseEntity.status(HttpStatus.CREATED).body(createdAppointment);
		} catch (AppointmentService.AppointmentServiceException e) {
			logger.error("Error al crear cita: email={}, mensaje={}", email, e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage()));
		} catch (Exception e) {
			logger.error("Error inesperado al crear cita: email={}", email, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Error interno al crear cita"));
		}
	}

	@PostMapping("/appointments/import")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> importGoogleEvents(@RequestParam String email) {
		logger.info("Importando eventos de Google Calendar para el email: {}", email);
		try {
			if (email == null || email.trim().isEmpty()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "El correo electrónico es obligatorio"));
			}
			appointmentService.importGoogleEvents(email);
			return ResponseEntity.ok().build();
		} catch (AppointmentService.AppointmentServiceException e) {
			logger.error("Error al importar eventos: email={}, mensaje={}", email, e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/appointments")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getAppointments(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
		logger.info("Obteniendo citas entre: {} y {}", start, end);
		try {
			List<Appointment> appointments = appointmentService.getAppointments(start, end);
			return ResponseEntity.ok(appointments);
		} catch (AppointmentService.AppointmentServiceException e) {
			logger.error("Error al obtener citas: mensaje={}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage()));
		}
	}

	@PutMapping("/appointments/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> updateAppointment(
			@PathVariable Long id,
			@Valid @RequestBody Appointment appointment) {
		logger.info("Actualizando cita: id={}", id);
		try {
			Appointment updatedAppointment = appointmentService.updateAppointment(id, appointment);
			return ResponseEntity.ok(updatedAppointment);
		} catch (AppointmentService.AppointmentServiceException e) {
			logger.error("Error al actualizar cita: id={}, mensaje={}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage()));
		}
	}

	@DeleteMapping("/appointments/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
		logger.info("Eliminando cita: id={}", id);
		try {
			appointmentService.deleteAppointment(id);
			return ResponseEntity.noContent().build();
		} catch (AppointmentService.AppointmentServiceException e) {
			logger.error("Error al eliminar cita: id={}, mensaje={}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/admin/appointments/{id}/sync-google")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> syncToGoogleCalendar(
			@PathVariable Long id,
			@RequestParam String email) throws IOException {
		logger.info("Sincronizando cita con Google Calendar: id={}, email={}", id, email);
		try {
			if (email == null || email.trim().isEmpty()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "El correo electrónico es obligatorio"));
			}
			Appointment appointment = appointmentService.findById(id);
			Appointment syncedAppointment = appointmentService.syncToGoogleCalendar(appointment, email);
			return ResponseEntity.ok(syncedAppointment);
		} catch (AppointmentService.AppointmentServiceException e) {
			logger.error("Error al sincronizar cita: id={}, email={}, mensaje={}", id, email, e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/admin/appointments/{id}/reminder")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> sendManualReminder(@PathVariable Long id) {
		logger.info("Enviando recordatorio manual: id={}", id);
		try {
			appointmentService.sendManualReminder(id);
			return ResponseEntity.ok().build();
		} catch (AppointmentService.AppointmentServiceException e) {
			logger.error("Error al enviar recordatorio: id={}, mensaje={}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage()));
		}
	}
}