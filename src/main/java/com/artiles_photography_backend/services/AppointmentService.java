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

package com.artiles_photography_backend.services;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artiles_photography_backend.models.Appointment;
import com.artiles_photography_backend.repository.AppointmentRepository;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import jakarta.mail.MessagingException;

/**
 * Servicio para la gestión de citas y su sincronización con Google Calendar.
 *
 * @author arojas
 */
@Service
@Transactional
public class AppointmentService {
	private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);
	private static final String TIME_ZONE = "America/Santo_Domingo"; // Zona horaria para Google Calendar
	private final AppointmentRepository appointmentRepository;
	private final GoogleAuthService googleAuthService;
	private final EmailService emailService;

	public AppointmentService(AppointmentRepository appointmentRepository, GoogleAuthService googleAuthService,
			EmailService emailService) {
		this.appointmentRepository = appointmentRepository;
		this.googleAuthService = googleAuthService;
		this.emailService = emailService;
	}

	public static class AppointmentServiceException extends RuntimeException {
		public AppointmentServiceException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	@Transactional
	public Appointment createAppointment(Appointment appointment, String email) {
		validateAppointment(appointment, email);
		appointment.setClientEmail(email);
		// No convertir fechas, asumir que ya están en UTC
		Appointment savedAppointment = appointmentRepository.save(appointment);
		logger.info("Cita creada en la base de datos: id={}, email={}", savedAppointment.getId(), email);

		try {
			syncToGoogleCalendar(savedAppointment, email);
		} catch (IOException e) {
			logger.error("Error al sincronizar con Google Calendar: id={}, email={}", savedAppointment.getId(), email,
					e);
			logger.warn("Cita creada sin sincronización con Google Calendar: id={}", savedAppointment.getId());
		}
		return savedAppointment;
	}

	@Transactional
	public Appointment updateAppointment(Long id, Appointment appointment) {
		Appointment existing = appointmentRepository.findById(id)
				.orElseThrow(() -> new AppointmentServiceException("Cita no encontrada: " + id, null));
		validateAppointment(appointment, appointment.getClientEmail());

		existing.setTitle(appointment.getTitle());
		existing.setDescription(appointment.getDescription());
		existing.setLocation(appointment.getLocation());
		// No convertir fechas, asumir que ya están en UTC
		existing.setStartTime(appointment.getStartTime());
		existing.setEndTime(appointment.getEndTime());
		existing.setClientName(appointment.getClientName());
		existing.setClientEmail(appointment.getClientEmail());
		existing.setReminderSent(appointment.isReminderSent());

		Appointment updatedAppointment = appointmentRepository.save(existing);
		logger.info("Cita actualizada en la base de datos: id={}, email={}", id, existing.getClientEmail());

		try {
			syncToGoogleCalendar(updatedAppointment, existing.getClientEmail());
			logger.info("Cita sincronizada con Google Calendar: id={}, email={}", id, existing.getClientEmail());
		} catch (IOException e) {
			logger.error("Error al sincronizar con Google Calendar: id={}, email={}", id, existing.getClientEmail(), e);
			logger.warn("Cita actualizada sin sincronización con Google Calendar: id={}", id);
		}

		return updatedAppointment;
	}

	@Transactional
	public void deleteAppointment(Long id) {
		Appointment appointment = appointmentRepository.findById(id)
				.orElseThrow(() -> new AppointmentServiceException("Cita no encontrada: " + id, null));
		if (appointment.getGoogleEventId() != null) {
			try {
				Calendar calendarClient = googleAuthService.getCalendarClient(appointment.getClientEmail());
				calendarClient.events().delete("primary", appointment.getGoogleEventId()).execute();
				logger.info("Evento eliminado en Google Calendar: eventId={}, email={}", appointment.getGoogleEventId(),
						appointment.getClientEmail());
			} catch (IOException e) {
				logger.error("Error al eliminar evento en Google Calendar: eventId={}, email={}",
						appointment.getGoogleEventId(), appointment.getClientEmail(), e);
				logger.warn("Evento no eliminado en Google Calendar: id={}", id);
			}
		}
		appointmentRepository.deleteById(id);
		logger.info("Cita eliminada de la base de datos: id={}", id);
	}

	public List<Appointment> getAppointments(LocalDateTime start, LocalDateTime end) {
		if (start == null || end == null || end.isBefore(start)) {
			throw new AppointmentServiceException("Fechas de inicio y fin inválidas", null);
		}
		// No convertir fechas, asumir que start y end están en UTC
		return appointmentRepository.findByStartTimeBetween(start, end);
	}

	public List<Appointment> getAppointmentsForReminders(LocalDateTime reminderTime) {
		if (reminderTime == null) {
			throw new AppointmentServiceException("Fecha de recordatorio inválida", null);
		}
		// No convertir fechas, asumir que reminderTime está en UTC
		logger.info("Obteniendo citas para recordatorios antes de: {}", reminderTime);
		return appointmentRepository.findByReminderSentFalseAndStartTimeBefore(reminderTime);
	}

	public List<Event> getAllEvents(String email) throws IOException {
		logger.info("Obteniendo todos los eventos para el email: {}", email);
		Calendar service = googleAuthService.getCalendarClient(email);
		Events events = service.events().list("primary")
				.setTimeMin(new DateTime(System.currentTimeMillis()))
				.setMaxResults(100)
				.execute();
		List<Event> items = events.getItems();
		logger.debug("Eventos obtenidos: count={}, email={}", items.size(), email);
		return items;
	}

	@Transactional
	public void importGoogleEvents(String email) {
		try {
			List<Event> googleEvents = getAllEvents(email);
			Set<String> existingEventIds = appointmentRepository.findAllGoogleEventIds();
			for (Event event : googleEvents) {
				if (!existingEventIds.contains(event.getId())) {
					Appointment appointment = new Appointment();
					appointment.setTitle(event.getSummary() != null ? event.getSummary() : "Sin título");
					appointment.setDescription(event.getDescription() != null ? event.getDescription() : "");
					appointment.setLocation(event.getLocation() != null ? event.getLocation() : "");
					appointment.setClientEmail(email);
					appointment.setClientName("Importado de Google");
					appointment.setGoogleEventId(event.getId());

					if (event.getStart() != null && event.getStart().getDateTime() != null) {
						appointment.setStartTime(LocalDateTime.ofInstant(
								Instant.ofEpochMilli(event.getStart().getDateTime().getValue()),
								ZoneId.of("UTC")));
					} else {
						logger.warn("Evento sin fecha de inicio: eventId={}, email={}", event.getId(), email);
						continue;
					}
					if (event.getEnd() != null && event.getEnd().getDateTime() != null) {
						appointment.setEndTime(LocalDateTime.ofInstant(
								Instant.ofEpochMilli(event.getEnd().getDateTime().getValue()),
								ZoneId.of("UTC")));
					} else {
						logger.warn("Evento sin fecha de fin: eventId={}, email={}", event.getId(), email);
						continue;
					}

					appointmentRepository.save(appointment);
					logger.info("Evento importado a la base de datos: eventId={}, email={}", event.getId(), email);
				}
			}
		} catch (IOException e) {
			logger.error("Error al importar eventos de Google Calendar: email={}", email, e);
			throw new AppointmentServiceException("Error al importar eventos", e);
		}
	}

	@Transactional
	public Appointment syncToGoogleCalendar(Appointment appointment, String email) throws IOException {
		if (appointment == null) {
			throw new AppointmentServiceException("La cita no puede ser null", null);
		}
		validateAppointment(appointment, email);

		Calendar calendarClient = googleAuthService.getCalendarClient(email);
		Event event = createGoogleEvent(appointment);

		if (appointment.getGoogleEventId() != null && !appointment.getGoogleEventId().isEmpty()) {
			try {
				Event updatedEvent = calendarClient.events().update("primary", appointment.getGoogleEventId(), event)
						.execute();
				logger.info("Evento actualizado en Google Calendar: eventId={}, email={}",
						appointment.getGoogleEventId(), email);
				appointment.setGoogleEventId(updatedEvent.getId());
			} catch (IOException e) {
				logger.error("Error al actualizar evento en Google Calendar: eventId={}, email={}",
						appointment.getGoogleEventId(), email, e);
				throw new IOException("Error al actualizar evento en Google Calendar: " + e.getMessage(), e);
			}
		} else {
			Event createdEvent = calendarClient.events().insert("primary", event).execute();
			appointment.setGoogleEventId(createdEvent.getId());
			logger.info("Cita sincronizada con Google Calendar: eventId={}, email={}", createdEvent.getId(), email);
		}

		appointment.setClientEmail(email);
		return appointmentRepository.save(appointment);
	}

	@Transactional
	public void sendManualReminder(Long id) {
		if (id == null) {
			throw new AppointmentServiceException("El ID de la cita no puede ser null", null);
		}
		Appointment appointment = appointmentRepository.findById(id)
				.orElseThrow(() -> new AppointmentServiceException("Cita no encontrada: " + id, null));
		if (appointment.isReminderSent()) {
			throw new AppointmentServiceException("El recordatorio ya fue enviado para esta cita", null);
		}
		try {
			emailService.sendAppointmentReminder(appointment);
			appointment.setReminderSent(true);
			appointmentRepository.save(appointment);
			logger.info("Recordatorio manual enviado: id={}, email={}", id, appointment.getClientEmail());
		} catch (MessagingException e) {
			logger.error("Error al enviar recordatorio manual: id={}, email={}", id, appointment.getClientEmail(), e);
			throw new AppointmentServiceException("Error al enviar el recordatorio", e);
		}
	}

	public Appointment findById(Long id) {
		if (id == null) {
			throw new AppointmentServiceException("El ID de la cita no puede ser null", null);
		}
		return appointmentRepository.findById(id)
				.orElseThrow(() -> new AppointmentServiceException("Cita no encontrada: " + id, null));
	}

	private void validateAppointment(Appointment appointment, String email) {
		if (appointment == null) {
			throw new AppointmentServiceException("La cita no puede ser null", null);
		}
		if (appointment.getTitle() == null || appointment.getTitle().trim().isEmpty()) {
			throw new AppointmentServiceException("El título de la cita es obligatorio", null);
		}
		if (appointment.getStartTime() == null) {
			throw new AppointmentServiceException("La fecha de inicio es obligatoria", null);
		}
		if (appointment.getEndTime() == null) {
			throw new AppointmentServiceException("La fecha de fin es obligatoria", null);
		}
		if (appointment.getEndTime().isBefore(appointment.getStartTime())) {
			throw new AppointmentServiceException("La fecha de fin debe ser posterior a la fecha de inicio", null);
		}
		if (email == null || email.trim().isEmpty()) {
			throw new AppointmentServiceException("El correo del cliente es obligatorio", null);
		}
		if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
			throw new AppointmentServiceException("Formato de correo electrónico inválido", null);
		}
	}

	private Event createGoogleEvent(Appointment appointment) {
		Event event = new Event()
				.setSummary(appointment.getTitle())
				.setDescription(appointment.getDescription() != null ? appointment.getDescription() : "")
				.setLocation(appointment.getLocation() != null ? appointment.getLocation() : "");

		// Convertir fechas de UTC a America/Santo_Domingo para Google Calendar
		LocalDateTime startTime = appointment.getStartTime().atZone(ZoneId.of("UTC"))
				.withZoneSameInstant(ZoneId.of(TIME_ZONE)).toLocalDateTime();
		LocalDateTime endTime = appointment.getEndTime().atZone(ZoneId.of("UTC"))
				.withZoneSameInstant(ZoneId.of(TIME_ZONE)).toLocalDateTime();

		EventDateTime start = new EventDateTime()
				.setDateTime(new DateTime(
						startTime.atZone(ZoneId.of(TIME_ZONE)).toInstant().toEpochMilli()))
				.setTimeZone(TIME_ZONE);
		event.setStart(start);

		EventDateTime end = new EventDateTime()
				.setDateTime(new DateTime(
						endTime.atZone(ZoneId.of(TIME_ZONE)).toInstant().toEpochMilli()))
				.setTimeZone(TIME_ZONE);
		event.setEnd(end);

		return event;
	}
}