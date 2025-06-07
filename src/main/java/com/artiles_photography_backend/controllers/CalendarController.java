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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
@RequestMapping("/api")
public class CalendarController {
	private static final Logger logger = LoggerFactory.getLogger(CalendarController.class); // Declarar logger
	private final GoogleAuthService googleAuthService;
	private final AppointmentService appointmentService;

	public CalendarController(GoogleAuthService googleAuthService, AppointmentService appointmentService) {
		this.googleAuthService = googleAuthService;
		this.appointmentService = appointmentService;
	}

	/**
	 * Inicia el flujo de autenticación con Google para el email proporcionado.
	 *
	 * @param email El email del usuario para autenticar con Google.
	 * @return ResponseEntity con la URL de autenticación o un mensaje de error si
	 *         falta el parámetro email.
	 */
	@GetMapping("/auth/google")
	public ResponseEntity<String> startAuth(@RequestParam(value = "email", required = false) String email) {
		if (email == null || email.trim().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("{\"error\": \"El parámetro 'email' es requerido.\"}");
		}
		try {
			String authUrl = googleAuthService.startAuthFlow(email);
			return ResponseEntity.ok(authUrl);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("{\"error\": \"Error al iniciar el flujo de autenticación: " + e.getMessage() + "\"}");
		}
	}

	@PostMapping("/auth/google")
	public ResponseEntity<String> startAuth(@RequestBody Map<String, String> requestBody) {
		String email = requestBody.get("email");
		if (email == null || email.trim().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("{\"error\": \"El campo 'email' es requerido en el cuerpo de la solicitud.\"}");
		}
		try {
			String authUrl = googleAuthService.startAuthFlow(email);
			return ResponseEntity.ok(authUrl);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("{\"error\": \"Error al iniciar el flujo de autenticación: " + e.getMessage() + "\"}");
		}
	}

	/**
	 * /**
	 * Maneja el callback de Google después de la autenticación.
	 *
	 * @param code  El código de autorización retornado por Google.
	 * @param email El email del usuario autenticado.
	 * @return ResponseEntity con un mensaje de éxito o error.
	 * @throws IOException Si ocurre un error al procesar el callback.
	 *                     @GetMapping("/callback")
	 *                     public ResponseEntity<String>
	 *                     handleCallback(@RequestParam String code, @RequestParam
	 *                     String email)
	 *                     throws IOException {
	 *                     try {
	 *                     googleAuthService.handleCallback(code, email);
	 *                     return ResponseEntity.ok("Authentication successful for "
	 *                     + email);
	 *                     } catch (IOException e) {
	 *                     return
	 *                     ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	 *                     .body("{\"error\": \"Error en el callback de
	 *                     autenticación: " + e.getMessage() + "\"}");
	 *                     }
	 *                     }
	 **/

	/**
	 * Cierra la sesión del usuario en Google.
	 *
	 * @param email El email del usuario para cerrar sesión.
	 * @return ResponseEntity con un mensaje de éxito o error.
	 */
	@PostMapping("/auth/signout")
	public ResponseEntity<String> signOut(@RequestParam String email) {
		try {
			googleAuthService.signOut(email);
			return ResponseEntity.ok("Signed out for " + email);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("{\"error\": \"Error al cerrar sesión: " + e.getMessage() + "\"}");
		}
	}

	/**
	 * Obtiene los eventos del calendario de Google para el email proporcionado.
	 *
	 * @param email El email del usuario para obtener los eventos.
	 * @return ResponseEntity con la lista de eventos o un mensaje de error.
	 * @throws IOException Si ocurre un error al acceder al calendario.
	 */
	@GetMapping("/calendar/events")
	public ResponseEntity<?> getEvents(@RequestParam String email) {
		logger.info("Fetching events for email: {}", email);
		try {
			List<Event> events = appointmentService.getAllEvents(email);
			return ResponseEntity.ok(events);
		} catch (IOException e) {
			logger.error("IOException fetching events for email: {}", email, e);
			if (e.getMessage().contains("No credentials found")) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("error", "Credenciales no encontradas para el email: " + email));
			}
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Error al interactuar con Google Calendar: " + e.getMessage()));
		} catch (Exception e) {
			logger.error("Unexpected error fetching events for email: {}", email, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Error inesperado: " + e.getMessage()));
		}
	}

	/**
	 * Maneja excepciones de parámetros faltantes.
	 *
	 * @param ex La excepción lanzada.
	 * @return ResponseEntity con un mensaje de error.
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
		String message = String.format("{\"error\": \"Parámetro requerido '%s' no está presente.\"}",
				ex.getParameterName());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
	}
}