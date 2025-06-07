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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.Appointment;
import com.artiles_photography_backend.repository.AppointmentRepository;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import jakarta.transaction.Transactional;

/**
 *
 * @author arojas
 *         Servicio para la gestión de citas y su sincronización con Google
 *         Calendar.
 */
@Service
public class AppointmentService {
	private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);
	private final AppointmentRepository appointmentRepository;
	private final GoogleAuthService googleAuthService;

	public AppointmentService(AppointmentRepository appointmentRepository, GoogleAuthService googleAuthService) {
		this.appointmentRepository = appointmentRepository;
		this.googleAuthService = googleAuthService;
	}

	@Transactional
	public Appointment createAppointment(Appointment appointment, String email) throws IOException {
		appointment.setClientEmail(email);
		Appointment savedAppointment = appointmentRepository.save(appointment);
		logger.info("Cita creada en la base de datos: {}", savedAppointment.getId());

		// Sincronizar con Google Calendar
		try {
			Calendar calendarClient = googleAuthService.getCalendarClient(email);
			Event event = new Event()
					.setSummary(appointment.getTitle())
					.setDescription(appointment.getDescription())
					.setLocation(appointment.getLocation());

			EventDateTime start = new EventDateTime()
					.setDateTime(new com.google.api.client.util.DateTime(
							appointment.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
					.setTimeZone(ZoneId.systemDefault().getId());
			event.setStart(start);

			EventDateTime end = new EventDateTime()
					.setDateTime(new com.google.api.client.util.DateTime(
							appointment.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
					.setTimeZone(ZoneId.systemDefault().getId());
			event.setEnd(end);

			Event createdEvent = calendarClient.events().insert("primary", event).execute();
			savedAppointment.setGoogleEventId(createdEvent.getId());
			appointmentRepository.save(savedAppointment);
			logger.info("Evento creado en Google Calendar: {}", createdEvent.getId());
		} catch (IOException e) {
			logger.error("Error al sincronizar con Google Calendar para la cita: {}", savedAppointment.getId(), e);
			throw e;
		}

		return savedAppointment;
	}

	@Transactional
	public Appointment updateAppointment(Long id, Appointment appointment) throws IOException {
		Appointment existing = appointmentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Cita no encontrada: " + id));
		existing.setTitle(appointment.getTitle());
		existing.setDescription(appointment.getDescription());
		existing.setLocation(appointment.getLocation());
		existing.setStartTime(appointment.getStartTime());
		existing.setEndTime(appointment.getEndTime());
		existing.setClientName(appointment.getClientName());
		existing.setClientEmail(appointment.getClientEmail());
		existing.setReminderSent(appointment.isReminderSent());

		// Actualizar en Google Calendar
		if (existing.getGoogleEventId() != null) {
			try {
				Calendar calendarClient = googleAuthService.getCalendarClient(existing.getClientEmail());
				Event event = new Event()
						.setSummary(appointment.getTitle())
						.setDescription(appointment.getDescription())
						.setLocation(appointment.getLocation());

				EventDateTime start = new EventDateTime()
						.setDateTime(new com.google.api.client.util.DateTime(
								appointment.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
						.setTimeZone(ZoneId.systemDefault().getId());
				event.setStart(start);

				EventDateTime end = new EventDateTime()
						.setDateTime(new com.google.api.client.util.DateTime(
								appointment.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
						.setTimeZone(ZoneId.systemDefault().getId());
				event.setEnd(end);

				calendarClient.events().update("primary", existing.getGoogleEventId(), event).execute();
				logger.info("Evento actualizado en Google Calendar: {}", existing.getGoogleEventId());
			} catch (IOException e) {
				logger.error("Error al actualizar en Google Calendar para la cita: {}", id, e);
				throw e;
			}
		}

		return appointmentRepository.save(existing);
	}

	@Transactional
	public void deleteAppointment(Long id) throws IOException {
		Appointment appointment = appointmentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Cita no encontrada: " + id));
		if (appointment.getGoogleEventId() != null) {
			try {
				Calendar calendarClient = googleAuthService.getCalendarClient(appointment.getClientEmail());
				calendarClient.events().delete("primary", appointment.getGoogleEventId()).execute();
				logger.info("Evento eliminado en Google Calendar: {}", appointment.getGoogleEventId());
			} catch (IOException e) {
				logger.error("Error al eliminar en Google Calendar para la cita: {}", id, e);
				throw e;
			}
		}
		appointmentRepository.deleteById(id);
		logger.info("Cita eliminada de la base de datos: {}", id);
	}

	public List<Appointment> getAppointments(LocalDateTime start, LocalDateTime end) {
		return appointmentRepository.findByStartTimeBetween(start, end);
	}

	public List<Event> getAllEvents(String email) throws IOException {
		Calendar calendarClient = googleAuthService.getCalendarClient(email);
		return calendarClient.events().list("primary")
				.setTimeMin(new com.google.api.client.util.DateTime(
						LocalDateTime.now().minusYears(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
				.setTimeMax(new com.google.api.client.util.DateTime(
						LocalDateTime.now().plusYears(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
				.execute().getItems();
	}

	@Transactional
	public void importGoogleEvents(String email) throws IOException {
		List<Event> googleEvents = getAllEvents(email);
		for (Event event : googleEvents) {
			if (appointmentRepository.findByGoogleEventId(event.getId()).isEmpty()) {
				Appointment appointment = new Appointment();
				appointment.setTitle(event.getSummary() != null ? event.getSummary() : "Sin título");
				appointment.setDescription(event.getDescription());
				appointment.setLocation(event.getLocation());
				appointment.setClientEmail(email);
				appointment.setClientName("Importado de Google");
				appointment.setGoogleEventId(event.getId());

				if (event.getStart().getDateTime() != null) {
					appointment.setStartTime(LocalDateTime.ofInstant(
							Instant.ofEpochMilli(event.getStart().getDateTime().getValue()),
							ZoneId.systemDefault()));
				} else {
					continue; // Ignorar eventos sin fecha específica
				}
				if (event.getEnd().getDateTime() != null) {
					appointment.setEndTime(LocalDateTime.ofInstant(
							Instant.ofEpochMilli(event.getEnd().getDateTime().getValue()),
							ZoneId.systemDefault()));
				} else {
					continue;
				}

				appointmentRepository.save(appointment);
				logger.info("Evento importado a la base de datos: {}", event.getId());
			}
		}
	}

	@Transactional
	public Appointment syncToGoogleCalendar(Long id, String email) throws IOException {
		Appointment appointment = appointmentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Cita no encontrada: " + id));
		if (appointment.getGoogleEventId() != null) {
			throw new RuntimeException("La cita ya está sincronizada con Google Calendar");
		}

		Calendar calendarClient = googleAuthService.getCalendarClient(email);
		Event event = new Event()
				.setSummary(appointment.getTitle())
				.setDescription(appointment.getDescription())
				.setLocation(appointment.getLocation());

		EventDateTime start = new EventDateTime()
				.setDateTime(new com.google.api.client.util.DateTime(
						appointment.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
				.setTimeZone(ZoneId.systemDefault().getId());
		event.setStart(start);

		EventDateTime end = new EventDateTime()
				.setDateTime(new com.google.api.client.util.DateTime(
						appointment.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
				.setTimeZone(ZoneId.systemDefault().getId());
		event.setEnd(end);

		Event createdEvent = calendarClient.events().insert("primary", event).execute();
		appointment.setGoogleEventId(createdEvent.getId());
		appointment.setClientEmail(email);
		appointmentRepository.save(appointment);
		logger.info("Cita sincronizada con Google Calendar: {}", createdEvent.getId());

		return appointment;
	}
}