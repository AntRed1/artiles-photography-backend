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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
 *
 * @author arojas
 *         Servicio para enviar correos electrónicos con plantillas
 *         personalizadas.
 */
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

	public void sendContactEmail(ContactMessage message) throws MessagingException {
		ContactInfoResponse contactInfo = getContactInfo();
		String logoUrl = getLogoUrl();
		String socialMediaLinks = getSocialMediaLinks(contactInfo);

		// Email to company
		sendEmail("CONTACT_MESSAGE_COMPANY", companyEmail, message.getEmail(), Map.of(
				"name", escapeHtml(message.getName()),
				"email", escapeHtml(message.getEmail()),
				"phone", escapeHtml(message.getPhone() != null ? message.getPhone() : "No proporcionado"),
				"service", escapeHtml(message.getService() != null ? message.getService() : "No especificado"),
				"message", escapeHtml(message.getMessage()),
				"createdAt", message.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a")),
				"clientIp", escapeHtml(message.getClientIp() != null ? message.getClientIp() : "Desconocido"),
				"userAgent", escapeHtml(message.getUserAgent() != null ? message.getUserAgent() : "Desconocido"),
				"logoUrl", logoUrl,
				"socialMediaLinks", socialMediaLinks));

		// Email to client
		sendEmail("CONTACT_MESSAGE_CLIENT", message.getEmail(), companyEmail, Map.of(
				"name", escapeHtml(message.getName()),
				"service", escapeHtml(message.getService() != null ? message.getService() : "No especificado"),
				"message", escapeHtml(message.getMessage()),
				"createdAt", message.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a")),
				"logoUrl", logoUrl,
				"socialMediaLinks", socialMediaLinks));
	}

	public void sendAppointmentReminder(Appointment appointment) throws MessagingException {
		ContactInfoResponse contactInfo = getContactInfo();
		String logoUrl = getLogoUrl();
		String socialMediaLinks = getSocialMediaLinks(contactInfo);

		sendEmail("APPOINTMENT_REMINDER", appointment.getClientEmail(), companyEmail, Map.of(
				"clientName", escapeHtml(appointment.getClientName()),
				"title", escapeHtml(appointment.getTitle()),
				"startTime", appointment.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a")),
				"location", escapeHtml(appointment.getLocation() != null ? appointment.getLocation() : ""),
				"description", escapeHtml(appointment.getDescription() != null ? appointment.getDescription() : ""),
				"logoUrl", logoUrl,
				"socialMediaLinks", socialMediaLinks));
	}

	public void sendCustomEmail(String from, String to, String subject, String date, String body)
			throws MessagingException {
		ContactInfoResponse contactInfo = getContactInfo();
		String logoUrl = getLogoUrl();
		String socialMediaLinks = getSocialMediaLinks(contactInfo);

		sendEmail("CUSTOM_EMAIL", to, from, Map.of(
				"subject", escapeHtml(subject),
				"date", escapeHtml(date),
				"body", escapeHtml(body),
				"logoUrl", logoUrl,
				"socialMediaLinks", socialMediaLinks));
	}

	private void sendEmail(String templateName, String to, String replyTo, Map<String, String> data)
			throws MessagingException {
		EmailTemplate template = emailTemplateRepository.findByTemplateName(templateName)
				.orElseThrow(() -> new RuntimeException("Email template not found: " + templateName));
		String htmlContent = template.getHtmlContent();
		String subject = data.getOrDefault("subject", template.getSubject());
		for (Map.Entry<String, String> entry : data.entrySet()) {
			htmlContent = htmlContent.replace("{{" + entry.getKey() + "}}", entry.getValue());
		}
		logger.debug("Contenido HTML final para el correo a {}: {}", to, htmlContent);
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setFrom(companyEmail);
		if (replyTo != null && !replyTo.isBlank()) {
			helper.setReplyTo(replyTo);
		}
		helper.setText(htmlContent, true);
		mailSender.send(message);
		logger.info("Correo enviado a {} usando la plantilla {}", to, templateName);
	}

	private ContactInfoResponse getContactInfo() {
		ContactInfoResponse contactInfo = contactInfoService.getContactInfo();
		if (contactInfo == null || contactInfo.getEmail() == null || contactInfo.getEmail().isBlank()) {
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
					"<a href='%s' style='margin: 0 10px;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/facebook.svg' alt='Facebook' width='24' height='24' style='vertical-align: middle;'></a>",
					facebookUrl));
		}
		if (!instagramUrl.isEmpty()) {
			socialMediaLinks.append(String.format(
					"<a href='%s' style='margin: 0 10px;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/instagram.svg' alt='Instagram' width='24' height='24' style='vertical-align: middle;'></a>",
					instagramUrl));
		}
		if (!twitterUrl.isEmpty()) {
			socialMediaLinks.append(String.format(
					"<a href='%s' style='margin: 0 10px;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/x.svg' alt='X' width='24' height='24' style='vertical-align: middle;'></a>",
					twitterUrl));
		}
		if (!tiktokUrl.isEmpty()) {
			socialMediaLinks.append(String.format(
					"<a href='%s' style='margin: 0 10px;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/tiktok.svg' alt='TikTok' width='24' height='24' style='vertical-align: middle;'></a>",
					tiktokUrl));
		}
		socialMediaLinks.append("</div>");
		return socialMediaLinks.toString();
	}

	private boolean validateUrl(String url) {
		return url != null && !url.trim().isEmpty() && (url.startsWith("http://") || url.startsWith("https://"));
	}

	private String escapeHtml(String input) {
		if (input == null) {
			return "";
		}
		return input.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
}