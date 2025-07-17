package com.artiles_photography_backend.services;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.Notification;
import com.artiles_photography_backend.repository.NotificationRepository;

/**
 * @author arojas
 *         Service for managing notifications, including creation and
 *         persistence.
 */
@Service
public class NotificationService {

	private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

	@Autowired
	private NotificationRepository notificationRepository;

	private static final List<String> VALID_ICONS = Arrays.asList("user-plus", "image", "exclamation-triangle");
	private static final List<String> VALID_COLORS = Arrays.asList("indigo", "green", "amber");

	/**
	 * Creates and saves a new notification.
	 *
	 * @param icon  The icon type (e.g., user-plus, image, exclamation-triangle)
	 * @param text  The notification message
	 * @param color The color theme (e.g., indigo, green, amber)
	 * @param link  Optional navigation link
	 * @throws IllegalArgumentException if input parameters are invalid
	 */
	public void createNotification(String icon, String text, String color, String link) {
		validateInputs(icon, text, color);

		Notification notification = new Notification();
		notification.setIcon(icon);
		notification.setText(text);
		notification.setTime(formatRelativeTime(LocalDateTime.now()));
		notification.setColor(color);
		notification.setLink(link);
		notification.setCreatedAt(LocalDateTime.now());

		notificationRepository.save(notification);
		logger.info("Notification created: text='{}', icon='{}', color='{}', link='{}'", text, icon, color, link);
	}

	/**
	 * Deletes a notification by ID.
	 *
	 * @param id The ID of the notification to delete
	 * @throws IllegalArgumentException if the notification does not exist
	 */
	public void deleteNotification(Long id) {
		logger.info("Deleting notification with id={}", id);
		if (!notificationRepository.existsById(id)) {
			logger.error("Notification with id={} not found", id);
			throw new IllegalArgumentException("Notification with id " + id + " not found");
		}
		notificationRepository.deleteById(id);
		logger.info("Notification with id={} deleted successfully", id);
	}

	/**
	 * Validates notification input parameters.
	 */
	private void validateInputs(String icon, String text, String color) {
		if (icon == null || !VALID_ICONS.contains(icon)) {
			logger.error("Invalid icon: {}. Valid icons: {}", icon, VALID_ICONS);
			throw new IllegalArgumentException("Invalid icon. Must be one of: " + VALID_ICONS);
		}
		if (text == null || text.trim().isEmpty() || text.length() > 255) {
			logger.error("Invalid text: {}", text);
			throw new IllegalArgumentException("Text must be non-empty and less than 255 characters");
		}
		if (color == null || !VALID_COLORS.contains(color)) {
			logger.error("Invalid color: {}. Valid colors: {}", color, VALID_COLORS);
			throw new IllegalArgumentException("Invalid color. Must be one of: " + VALID_COLORS);
		}
	}

	/**
	 * Formats the time as a relative string (e.g., "Hace 5 minutos").
	 */
	private String formatRelativeTime(LocalDateTime createdAt) {
		LocalDateTime now = LocalDateTime.now();
		long minutes = ChronoUnit.MINUTES.between(createdAt, now);
		long hours = ChronoUnit.HOURS.between(createdAt, now);
		long days = ChronoUnit.DAYS.between(createdAt, now);

		if (minutes < 1) {
			return "Hace unos momentos";
		} else if (minutes < 60) {
			return String.format("Hace %d minuto%s", minutes, minutes == 1 ? "" : "s");
		} else if (hours < 24) {
			return String.format("Hace %d hora%s", hours, hours == 1 ? "" : "s");
		} else {
			return String.format("Hace %d día%s", days, days == 1 ? "" : "s");
		}
	}
}