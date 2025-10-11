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

import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.dtos.ConfigurationResponse;
import com.artiles_photography_backend.dtos.ContactInfoResponse;
import com.artiles_photography_backend.models.Appointment;
import com.artiles_photography_backend.models.ContactMessage;
import com.artiles_photography_backend.models.EmailTemplate;
import com.artiles_photography_backend.repository.EmailTemplateRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service for sending emails using database-stored templates.
 *
 * @author arojas
 */
@Service
public class EmailService {
	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
	private static final String FALLBACK_LOGO_URL = "https://artilesphotography.com/assets/placeholder-logo.png";

	private final JavaMailSender mailSender;
	private final EmailTemplateRepository emailTemplateRepository;
	private final ContactInfoService contactInfoService;
	private final ConfigurationService configurationService;

	@Value("${spring.mail.username}")
	private String companyEmail;

	public EmailService(
			JavaMailSender mailSender,
			EmailTemplateRepository emailTemplateRepository,
			ContactInfoService contactInfoService,
			ConfigurationService configurationService) {
		this.mailSender = mailSender;
		this.emailTemplateRepository = emailTemplateRepository;
		this.contactInfoService = contactInfoService;
		this.configurationService = configurationService;
	}

	@Async
	public void sendContactEmail(ContactMessage message) throws MessagingException {
		ContactInfoResponse contactInfo = getContactInfo();
		String logoUrl = getLogoUrl();
		String socialMediaLinks = getSocialMediaLinks(contactInfo);

		// Email to company
		sendEmail("CONTACT_MESSAGE_COMPANY", companyEmail, message.getEmail(), Map.of(
				"name", safeValue(message.getName()),
				"email", safeValue(message.getEmail()),
				"phone", safeValue(message.getPhone() != null ? message.getPhone() : "No proporcionado"),
				"service", safeValue(message.getService() != null ? message.getService() : "No especificado"),
				"message", safeValue(message.getMessage()),
				"createdAt", message.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a")),
				"clientIp", safeValue(message.getClientIp() != null ? message.getClientIp() : "Desconocido"),
				"userAgent", safeValue(message.getUserAgent() != null ? message.getUserAgent() : "Desconocido"),
				"logoUrl", logoUrl,
				"socialMediaLinks", socialMediaLinks));

		// Email to client
		sendEmail("CONTACT_MESSAGE_CLIENT", message.getEmail(), companyEmail, Map.of(
				"name", safeValue(message.getName()),
				"service", safeValue(message.getService() != null ? message.getService() : "No especificado"),
				"message", safeValue(message.getMessage()),
				"createdAt", message.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a")),
				"logoUrl", logoUrl,
				"socialMediaLinks", socialMediaLinks));
	}

	public void sendAppointmentReminder(Appointment appointment) throws MessagingException {
		ContactInfoResponse contactInfo = getContactInfo();
		String logoUrl = getLogoUrl();
		String socialMediaLinks = getSocialMediaLinks(contactInfo);

		sendEmail("APPOINTMENT_REMINDER", appointment.getClientEmail(), companyEmail, Map.of(
				"clientName", safeValue(appointment.getClientName()),
				"title", safeValue(appointment.getTitle()),
				"startTime", appointment.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a")),
				"location", safeValue(appointment.getLocation() != null ? appointment.getLocation() : ""),
				"description", safeValue(appointment.getDescription() != null ? appointment.getDescription() : ""),
				"logoUrl", logoUrl,
				"socialMediaLinks", socialMediaLinks));
	}

	public void sendCustomEmail(String from, String to, String subject, String date, String body)
			throws MessagingException {
		ContactInfoResponse contactInfo = getContactInfo();
		String logoUrl = getLogoUrl();
		String socialMediaLinks = getSocialMediaLinks(contactInfo);

		sendEmail("CUSTOM_EMAIL", to, from, Map.of(
				"subject", safeValue(subject),
				"date", safeValue(date),
				"body", safeValue(body),
				"logoUrl", logoUrl,
				"socialMediaLinks", socialMediaLinks));
	}

	private void sendEmail(String templateName, String to, String replyTo, Map<String, String> data)
			throws MessagingException {
		logger.info("Attempting to send email to {} using template {}", to, templateName);

		// Retrieve template from database
		EmailTemplate template = emailTemplateRepository.findByTemplateName(templateName)
				.orElseThrow(() -> {
					logger.error("Email template not found: {}", templateName);
					return new RuntimeException("Email template not found: " + templateName);
				});

		String htmlContent = template.getHtmlContent();
		if (htmlContent == null || htmlContent.trim().isEmpty()) {
			logger.error("Template {} has empty or null HTML content", templateName);
			throw new RuntimeException("Template " + templateName + " has empty or null HTML content");
		}

		logger.debug("Retrieved template {} with content length: {}", templateName, htmlContent.length());

		// Validate HTML content
		if (!htmlContent.contains("<!DOCTYPE html") || !htmlContent.contains("<html")) {
			logger.warn("Template {} does not appear to be valid HTML", templateName);
		}

		// Replace placeholders
		String subject = data.getOrDefault("subject", template.getSubject());
		String finalHtmlContent = htmlContent;
		for (Map.Entry<String, String> entry : data.entrySet()) {
			String placeholder = "{{" + entry.getKey() + "}}";
			String value = entry.getValue();
			finalHtmlContent = finalHtmlContent.replace(placeholder, value);
			logger.debug("Replaced placeholder {} with value {}", placeholder, value);
		}

		// Check for unreplaced placeholders
		if (finalHtmlContent.matches(".*\\{\\{[^}]+\\}\\}.*")) {
			logger.warn("Unreplaced placeholders found in final HTML content for template {}", templateName);
		}

		// Create plain text fallback
		String plainTextContent;
		if ("CUSTOM_EMAIL".equals(templateName)) {
			plainTextContent = "Estimado/a Cliente,\n\n" +
					"Gracias por su interés en Artiles Photography Studio.\n\n" +
					"Asunto: " + safeValue(subject) + "\n" +
					"Fecha: " + safeValue(data.getOrDefault("date", "No especificada")) + "\n" +
					"Mensaje: " + safeValue(data.getOrDefault("body", "")) + "\n\n" +
					"Contáctenos en https://artilesphotography.com/contact\n\n" +
					"Artiles Photography Studio\n" +
					"https://artilesphotography.com";
		} else if ("CONTACT_MESSAGE_CLIENT".equals(templateName)) {
			plainTextContent = "Estimado/a " + safeValue(data.getOrDefault("name", "Cliente")) + ",\n\n" +
					"Gracias por contactar con Artiles Photography Studio. Hemos recibido su mensaje:\n" +
					"Servicio: " + safeValue(data.getOrDefault("service", "No especificado")) + "\n" +
					"Mensaje: " + safeValue(data.getOrDefault("message", "")) + "\n" +
					"Fecha: " + safeValue(data.getOrDefault("createdAt", "")) + "\n\n" +
					"Te contactaremos pronto.\n\n" +
					"Artiles Photography Studio\n" +
					"https://artilesphotography.com";
		} else if ("APPOINTMENT_REMINDER".equals(templateName)) {
			plainTextContent = "Estimado/a " + safeValue(data.getOrDefault("clientName", "Cliente")) + ",\n\n" +
					"Este es un recordatorio de su cita con Artiles Photography Studio:\n" +
					"Título: " + safeValue(data.getOrDefault("title", "")) + "\n" +
					"Fecha y Hora: " + safeValue(data.getOrDefault("startTime", "")) + "\n" +
					"Ubicación: " + safeValue(data.getOrDefault("location", "")) + "\n" +
					"Descripción: " + safeValue(data.getOrDefault("description", "")) + "\n\n" +
					"Contáctenos en https://artilesphotography.com/contact\n\n" +
					"Artiles Photography Studio\n" +
					"https://artilesphotography.com";
		} else {
			plainTextContent = "Mensaje de Artiles Photography Studio\n\n" +
					"Gracias por contactarnos. Te responderemos pronto.\n\n" +
					"Artiles Photography Studio\n" +
					"https://artilesphotography.com";
		}

		// Log the final HTML content for debugging
		logger.debug("Final HTML content for email to {}: {}", to, finalHtmlContent);

		// Send email
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setFrom(companyEmail);
		if (replyTo != null && !replyTo.isBlank()) {
			helper.setReplyTo(replyTo);
		}
		helper.setText(plainTextContent, finalHtmlContent); // Set plain text and HTML content
		mailSender.send(message);
		logger.info("Email sent successfully to {} using template {}", to, templateName);
	}

	private ContactInfoResponse getContactInfo() {
		ContactInfoResponse contactInfo = contactInfoService.getContactInfo();
		if (contactInfo == null || contactInfo.getEmail() == null || contactInfo.getEmail().isBlank()) {
			logger.error("Contact information not found");
			throw new RuntimeException("Contact information not found");
		}
		return contactInfo;
	}

	private String getLogoUrl() {
		try {
			ConfigurationResponse config = configurationService.getConfiguration();
			return config != null && config.getLogoUrl() != null && !config.getLogoUrl().isBlank()
					? config.getLogoUrl()
					: FALLBACK_LOGO_URL;
		} catch (Exception e) {
			logger.error("Error fetching logo: {}", e.getMessage(), e);
			return FALLBACK_LOGO_URL;
		}
	}

	private String getSocialMediaLinks(ContactInfoResponse contactInfo) {
		String facebookUrl = validateUrl(contactInfo.getFacebook()) ? contactInfo.getFacebook() : "";
		String instagramUrl = validateUrl(contactInfo.getInstagram()) ? contactInfo.getInstagram() : "";
		String twitterUrl = validateUrl(contactInfo.getTwitter()) ? contactInfo.getTwitter() : "";
		String tiktokUrl = validateUrl(contactInfo.getTiktok()) ? contactInfo.getTiktok() : "";

		StringBuilder socialMediaLinks = new StringBuilder(
				"<div class='social-media' style='text-align: center; margin-top: 20px;'>");
		if (!facebookUrl.isEmpty()) {
			socialMediaLinks.append(String.format(
					"<a href='%s' style='margin: 0 8px; width: 32px; height: 32px; background-color: #333333; border-radius: 50%%; text-align: center; line-height: 32px; display: inline-block; transition: background-color 0.3s ease;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v13/icons/facebook.svg' alt='Facebook' width='20' height='20' style='vertical-align: middle;'></a>",
					facebookUrl));
		}
		if (!instagramUrl.isEmpty()) {
			socialMediaLinks.append(String.format(
					"<a href='%s' style='margin: 0 8px; width: 32px; height: 32px; background-color: #333333; border-radius: 50%%; text-align: center; line-height: 32px; display: inline-block; transition: background-color 0.3s ease;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v13/icons/instagram.svg' alt='Instagram' width='20' height='20' style='vertical-align: middle;'></a>",
					instagramUrl));
		}
		if (!twitterUrl.isEmpty()) {
			socialMediaLinks.append(String.format(
					"<a href='%s' style='margin: 0 8px; width: 32px; height: 32px; background-color: #333333; border-radius: 50%%; text-align: center; line-height: 32px; display: inline-block; transition: background-color 0.3s ease;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v13/icons/x.svg' alt='X' width='20' height='20' style='vertical-align: middle;'></a>",
					twitterUrl));
		}
		if (!tiktokUrl.isEmpty()) {
			socialMediaLinks.append(String.format(
					"<a href='%s' style='margin: 0 8px; width: 32px; height: 32px; background-color: #333333; border-radius: 50%%; text-align: center; line-height: 32px; display: inline-block; transition: background-color 0.3s ease;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v13/icons/tiktok.svg' alt='TikTok' width='20' height='20' style='vertical-align: middle;'></a>",
					tiktokUrl));
		}
		socialMediaLinks.append("</div>");
		return socialMediaLinks.toString();
	}

	private boolean validateUrl(String url) {
		return url != null && !url.trim().isEmpty() && (url.startsWith("http://") || url.startsWith("https://"));
	}

	private String safeValue(String input) {
		return input == null ? "" : StringEscapeUtils.escapeHtml4(input);
	}
}