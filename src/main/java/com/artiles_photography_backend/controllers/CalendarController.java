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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.services.AppointmentService;
import com.artiles_photography_backend.services.GoogleAuthService;
import com.google.api.services.calendar.model.Event;

/**
 * Controlador para manejar la autenticación con Google Calendar y la gestión de
 * eventos.
 *
 * @author arojas
 */
@RestController
@RequestMapping("/api/calendar")
public class CalendarController {
	private static final Logger logger = LoggerFactory.getLogger(CalendarController.class);
	private final GoogleAuthService googleAuthService;
	private final AppointmentService appointmentService;
	private final String frontendOrigin;

	public CalendarController(
			GoogleAuthService googleAuthService,
			AppointmentService appointmentService,
			@Value("${frontend.origin:http://localhost:5173}") String frontendOrigin) {
		this.googleAuthService = googleAuthService;
		this.appointmentService = appointmentService;
		this.frontendOrigin = frontendOrigin;
	}

	@GetMapping("/callback")
	public ResponseEntity<String> handleCallback(
			@RequestParam("code") String code,
			@RequestParam("state") String state) {
		logger.info("Procesando callback con code: [protegido], state: {}", state);
		try {
			googleAuthService.handleCallback(code, state);
			logger.info("Callback procesado exitosamente para el email: {}", state);
			String script = String.format(
					"<script>" +
							"window.opener.postMessage({type: 'GOOGLE_AUTH_SUCCESS', email: '%s'}, '%s');" +
							"window.close();" +
							"</script>",
					state, frontendOrigin);
			return ResponseEntity.ok()
					.contentType(MediaType.TEXT_HTML)
					.body(script);
		} catch (IllegalArgumentException e) {
			logger.error("Error de validación en callback para el email {}: {}", state, e.getMessage());
			String script = String.format(
					"<script>" +
							"window.opener.postMessage({type: 'GOOGLE_AUTH_ERROR', message: '%s'}, '%s');" +
							"window.close();" +
							"</script>",
					e.getMessage(), frontendOrigin);
			return ResponseEntity.badRequest()
					.contentType(MediaType.TEXT_HTML)
					.body(script);
		} catch (IOException e) {
			logger.error("Error procesando callback para el email {}: {}", state, e.getMessage());
			String script = String.format(
					"<script>" +
							"window.opener.postMessage({type: 'GOOGLE_AUTH_ERROR', message: '%s'}, '%s');" +
							"window.close();" +
							"</script>",
					"Error procesando la autenticación con Google: " + e.getMessage(), frontendOrigin);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.contentType(MediaType.TEXT_HTML)
					.body(script);
		}
	}

	@GetMapping("/events")
	public ResponseEntity<?> getEvents(@RequestParam String email) {
		logger.info("Obteniendo eventos para el email: {}", email);
		try {
			List<Event> events = appointmentService.getAllEvents(email);
			return ResponseEntity.ok(events);
		} catch (IOException e) {
			logger.error("Error al obtener eventos para el email: {}", email, e);
			if (e.getMessage().contains("No credentials found")) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("error", "Credenciales no encontradas para el email: " + email));
			}
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Error al interactuar con Google Calendar: " + e.getMessage()));
		} catch (IllegalArgumentException e) {
			logger.error("Error de validación al obtener eventos para el email: {}", email, e);
			return ResponseEntity.badRequest()
					.body(Map.of("error", "Email inválido: " + e.getMessage()));
		} catch (Exception e) {
			logger.error("Error inesperado al obtener eventos para el email: {}", email, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Error inesperado: " + e.getMessage()));
		}
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<Map<String, String>> handleMissingParams(MissingServletRequestParameterException ex) {
		logger.error("Parámetro requerido '{}' no está presente", ex.getParameterName());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error",
						String.format("Parámetro requerido '%s' no está presente", ex.getParameterName())));
	}
}