package com.artiles_photography_backend.services;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artiles_photography_backend.dtos.ConfigurationResponse;
import com.artiles_photography_backend.dtos.ContactInfoResponse;
import com.artiles_photography_backend.dtos.ContactMessageRequest;
import com.artiles_photography_backend.dtos.ContactMessageResponse;
import com.artiles_photography_backend.models.ContactMessage;
import com.artiles_photography_backend.repository.ContactMessageRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;

/**
 * @author arojas
 *         Servicio para gestionar mensajes de contacto, incluyendo guardado en
 *         base de datos
 *         y envío de notificaciones por correo electrónico con temática de
 *         fotografía.
 */
@Service
public class ContactMessageService {

	private static final Logger logger = LoggerFactory.getLogger(ContactMessageService.class);
	private static final String FALLBACK_LOGO_URL = "https://artilesphotography.com/assets/placeholder-logo.png";

	private final ContactMessageRepository contactMessageRepository;
	private final JavaMailSender mailSender;
	private final ContactInfoService contactInfoService;
	private final ConfigurationService configurationService;

	public ContactMessageService(
			ContactMessageRepository contactMessageRepository,
			JavaMailSender mailSender,
			ContactInfoService contactInfoService,
			ConfigurationService configurationService) {
		this.contactMessageRepository = contactMessageRepository;
		this.mailSender = mailSender;
		this.contactInfoService = contactInfoService;
		this.configurationService = configurationService;
	}

	public List<ContactMessageResponse> getAllContactMessages() {
		logger.debug("Obteniendo todos los mensajes de contacto");
		return contactMessageRepository.findAll().stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public ContactMessageResponse getContactMessageById(Long id) {
		logger.debug("Obteniendo mensaje de contacto con ID: {}", id);
		ContactMessage message = contactMessageRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Mensaje de contacto no encontrado con ID: " + id));
		return mapToResponse(message);
	}

	public List<ContactMessageResponse> getContactMessagesByEmail(String email) {
		logger.debug("Obteniendo mensajes de contacto por email: {}", email);
		return contactMessageRepository.findByEmail(email).stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public List<ContactMessageResponse> getContactMessagesByService(String service) {
		logger.debug("Obteniendo mensajes de contacto por servicio: {}", service);
		return contactMessageRepository.findByService(service).stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public ContactMessageResponse createContactMessage(ContactMessageRequest request, String clientIp,
			String userAgent) {
		logger.info("Creando nuevo mensaje de contacto desde IP: {}, User-Agent: {}", clientIp, userAgent);
		ContactMessage message = new ContactMessage();
		updateEntityFromRequest(message, request);
		message.setClientIp(clientIp);
		message.setUserAgent(userAgent);
		message = contactMessageRepository.save(message);

		// Enviar correo electrónico
		try {
			sendContactEmail(message);
			logger.info("Correos enviados exitosamente para el mensaje de contacto ID: {}", message.getId());
		} catch (MessagingException e) {
			logger.error("Error al enviar correos para el mensaje de contacto ID: {}: {}", message.getId(),
					e.getMessage(), e);
			// No lanzar excepción para no interrumpir el flujo, pero registrar el error
		}

		return mapToResponse(message);
	}

	@Transactional
	public ContactMessageResponse updateContactMessage(Long id, ContactMessageRequest request) {
		logger.debug("Actualizando mensaje de contacto con ID: {}", id);
		ContactMessage message = contactMessageRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Mensaje de contacto no encontrado con ID: " + id));
		updateEntityFromRequest(message, request);
		message = contactMessageRepository.save(message);
		return mapToResponse(message);
	}

	@Transactional
	public void deleteContactMessage(Long id) {
		logger.debug("Eliminando mensaje de contacto con ID: {}", id);
		if (!contactMessageRepository.existsById(id)) {
			throw new EntityNotFoundException("Mensaje de contacto no encontrado con ID: " + id);
		}
		contactMessageRepository.deleteById(id);
		logger.info("Mensaje de contacto eliminado con ID: {}", id);
	}

	private void updateEntityFromRequest(ContactMessage message, ContactMessageRequest request) {
		message.setName(request.getName());
		message.setEmail(request.getEmail());
		message.setPhone(request.getPhone());
		message.setService(request.getService());
		message.setMessage(request.getMessage());
	}

	private ContactMessageResponse mapToResponse(ContactMessage message) {
		ContactMessageResponse response = new ContactMessageResponse();
		response.setId(message.getId());
		response.setName(message.getName());
		response.setEmail(message.getEmail());
		response.setPhone(message.getPhone());
		response.setService(message.getService());
		response.setMessage(message.getMessage());
		response.setClientIp(message.getClientIp());
		response.setUserAgent(message.getUserAgent());
		response.setCreatedAt(message.getCreatedAt());
		return response;
	}

	// Made public to allow resending emails from the controller
	public void sendContactEmail(ContactMessage message) throws MessagingException {
		// Obtener información de contacto desde la base de datos
		ContactInfoResponse contactInfo = contactInfoService.getContactInfo();
		if (contactInfo == null || contactInfo.getEmail() == null || contactInfo.getEmail().isBlank()) {
			logger.warn(
					"No se encontró un correo electrónico de contacto en la base de datos. No se enviarán correos para el mensaje ID: {}",
					message.getId());
			return;
		}
		String companyEmail = contactInfo.getEmail();

		// Obtener logo desde ConfigurationService
		String logoUrl = FALLBACK_LOGO_URL;
		try {
			ConfigurationResponse config = configurationService.getConfiguration();
			if (config != null && config.getLogoUrl() != null && !config.getLogoUrl().isBlank()) {
				logoUrl = config.getLogoUrl();
				logger.debug("Logo obtenido desde ConfigurationService: {}", logoUrl);
			} else {
				logger.warn("No se encontró un logo válido en la configuración. Usando logo por defecto: {}", logoUrl);
			}
		} catch (Exception e) {
			logger.error("Error al obtener el logo desde ConfigurationService: {}", e.getMessage(), e);
			logger.info("Usando logo por defecto debido al error: {}", logoUrl);
		}

		// Validar y preparar enlaces de redes sociales
		String facebookUrl = validateUrl(contactInfo.getFacebook()) ? contactInfo.getFacebook() : "";
		String instagramUrl = validateUrl(contactInfo.getInstagram()) ? contactInfo.getInstagram() : "";
		String twitterUrl = validateUrl(contactInfo.getTwitter()) ? contactInfo.getTwitter() : "";
		String tiktokUrl = validateUrl(contactInfo.getTiktok()) ? contactInfo.getTiktok() : "";

		String socialMediaLinks = "";
		if (!facebookUrl.isEmpty() || !instagramUrl.isEmpty() || !twitterUrl.isEmpty() || !tiktokUrl.isEmpty()) {
			socialMediaLinks = "<div class='social-media' style='text-align: center; margin-top: 20px;'>";
			if (!facebookUrl.isEmpty()) {
				socialMediaLinks += String.format(
						"<a href='%s' style='margin: 0 10px;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/facebook.svg' alt='Facebook' width='24' height='24' style='vertical-align: middle;'></a>",
						facebookUrl);
			}
			if (!instagramUrl.isEmpty()) {
				socialMediaLinks += String.format(
						"<a href='%s' style='margin: 0 10px;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/instagram.svg' alt='Instagram' width='24' height='24' style='vertical-align: middle;'></a>",
						instagramUrl);
			}
			if (!twitterUrl.isEmpty()) {
				socialMediaLinks += String.format(
						"<a href='%s' style='margin: 0 10px;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/x.svg' alt='X' width='24' height='24' style='vertical-align: middle;'></a>",
						twitterUrl);
			}
			if (!tiktokUrl.isEmpty()) {
				socialMediaLinks += String.format(
						"<a href='%s' style='margin: 0 10px;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/tiktok.svg' alt='TikTok' width='24' height='24' style='vertical-align: middle;'></a>",
						tiktokUrl);
			}
			socialMediaLinks += "</div>";
		}

		// Enviar correo a la empresa
		MimeMessage companyMessage = mailSender.createMimeMessage();
		MimeMessageHelper companyHelper = new MimeMessageHelper(companyMessage, true, "UTF-8");

		companyHelper.setTo(companyEmail);
		companyHelper.setSubject("Nuevo Mensaje de Contacto - Artiles Photography Studio");
		companyHelper.setFrom(companyEmail);
		companyHelper.setReplyTo(message.getEmail());

		// Cuerpo del correo para la empresa
		String companyHtmlContent = """
				<!DOCTYPE html>
				<html>
				<head>
				    <meta charset="UTF-8">
				    <meta name="viewport" content="width=device-width, initial-scale=1.0">
				    <style>
				        body { font-family: 'Helvetica Neue', Arial, sans-serif; color: #333; line-height: 1.6; margin: 0; padding: 0; }
				        .container { max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 12px; background-color: #f9f9f9; }
				        .header { background-color: #333333; color: white; padding: 15px; text-align: center; border-radius: 12px 12px 0 0; }
				        .header img { max-width: 150px; height: auto; }
				        .content { padding: 20px; background-color: white; border-radius: 0 0 12px 12px; }
				        .field { margin-bottom: 15px; }
				        .field-label { font-weight: bold; color: #444; display: flex; align-items: center; }
				        .field-label::before { content: '📷'; margin-right: 8px; font-size: 16px; }
				        .field-value { margin: 5px 0 0 24px; color: #555; }
				        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #777; }
				        .footer a { color: #d4a017; text-decoration: none; }
				        .footer a:hover { text-decoration: underline; }
				        @media screen and (max-width: 600px) {
				            .container { margin: 10px; padding: 15px; }
				            .header img { max-width: 120px; }
				            .content { padding: 15px; }
				        }
				    </style>
				</head>
				<body>
				    <div class="container">
				        <div class="header">
				            <img src="%s" alt="Artiles Photography Studio">
				            <h2>Nuevo Mensaje de Contacto</h2>
				        </div>
				        <div class="content">
				            <div class="field">
				                <span class="field-label">Nombre:</span>
				                <p class="field-value">%s</p>
				            </div>
				            <div class="field">
				                <span class="field-label">Correo Electrónico:</span>
				                <p class="field-value">%s</p>
				            </div>
				            <div class="field">
				                <span class="field-label">Teléfono:</span>
				                <p class="field-value">%s</p>
				            </div>
				            <div class="field">
				                <span class="field-label">Servicio de Interés:</span>
				                <p class="field-value">%s</p>
				            </div>
				            <div class="field">
				                <span class="field-label">Mensaje:</span>
				                <p class="field-value">%s</p>
				            </div>
				            <div class="field">
				                <span class="field-label">Fecha de Envío:</span>
				                <p class="field-value">%s</p>
				            </div>
				            <div class="field">
				                <span class="field-label">IP del Cliente:</span>
				                <p class="field-value">%s</p>
				            </div>
				            <div class="field">
				                <span class="field-label">User Agent:</span>
				                <p class="field-value">%s</p>
				            </div>
				        </div>
				        <div class="footer">
				            <p>Enviado desde Artiles Photography Studio</p>
				            <p><a href="https://artilesphotography.com">artilesphotography.com</a></p>
				            %s
				        </div>
				    </div>
				</body>
				</html>
				"""
				.formatted(
						logoUrl,
						escapeHtml(message.getName()),
						escapeHtml(message.getEmail()),
						escapeHtml(message.getPhone() != null ? message.getPhone() : "No proporcionado"),
						escapeHtml(message.getService() != null ? message.getService() : "No especificado"),
						escapeHtml(message.getMessage()),
						message.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a")),
						escapeHtml(message.getClientIp() != null ? message.getClientIp() : "Desconocido"),
						escapeHtml(message.getUserAgent() != null ? message.getUserAgent() : "Desconocido"),
						socialMediaLinks);

		companyHelper.setText(companyHtmlContent, true);
		mailSender.send(companyMessage);
		logger.info("Correo enviado exitosamente a la empresa para el mensaje de contacto ID: {}", message.getId());

		// Enviar correo de confirmación al cliente
		MimeMessage clientMessage = mailSender.createMimeMessage();
		MimeMessageHelper clientHelper = new MimeMessageHelper(clientMessage, true, "UTF-8");

		clientHelper.setTo(message.getEmail());
		clientHelper.setSubject("Confirmación de Mensaje - Artiles Photography Studio");
		clientHelper.setFrom(companyEmail);

		// Cuerpo del correo para el cliente
		String clientHtmlContent = """
				<!DOCTYPE html>
				<html>
				<head>
				    <meta charset="UTF-8">
				    <meta name="viewport" content="width=device-width, initial-scale=1.0">
				    <style>
				        body { font-family: 'Helvetica Neue', Arial, sans-serif; color: #333; line-height: 1.6; margin: 0; padding: 0; }
				        .container { max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 12px; background-color: #f9f9f9; }
				        .header { background-color: #333333; color: white; padding: 15px; text-align: center; border-radius: 12px 12px 0 0; }
				        .header img { max-width: 150px; height: auto; }
				        .content { padding: 20px; background-color: white; border-radius: 0 0 12px 12px; }
				        .field { margin-bottom: 15px; }
				        .field-label { font-weight: bold; color: #444; display: flex; align-items: center; }
				        .field-label::before { content: '📷'; margin-right: 8px; font-size: 16px; }
				        .field-value { margin: 5px 0 0 24px; color: #555; }
				        .greeting { margin-bottom: 20px; }
				        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #777; }
				        .footer a { color: #d4a017; text-decoration: none; }
				        .footer a:hover { text-decoration: underline; }
				        @media screen and (max-width: 600px) {
				            .container { margin: 10px; padding: 15px; }
				            .header img { max-width: 120px; }
				            .content { padding: 15px; }
				        }
				    </style>
				</head>
				<body>
				    <div class="container">
				        <div class="header">
				            <img src="%s" alt="Artiles Photography Studio">
				            <h2>Confirmación de Mensaje</h2>
				        </div>
				        <div class="content">
				            <p class="greeting">Estimado/a %s,</p>
				            <p>Gracias por contactar con Artiles Photography Studio. Hemos recibido tu mensaje y te contactaremos pronto para discutir los detalles.</p>
				            <div class="field">
				                <span class="field-label">Servicio de Interés:</span>
				                <p class="field-value">%s</p>
				            </div>
				            <div class="field">
				                <span class="field-label">Mensaje:</span>
				                <p class="field-value">%s</p>
				            </div>
				            <div class="field">
				                <span class="field-label">Fecha de Envío:</span>
				                <p class="field-value">%s</p>
				            </div>
				            <p>Si tienes alguna pregunta adicional, no dudes en responder a este correo.</p>
				        </div>
				        <div class="footer">
				            <p>Artiles Photography Studio</p>
				            <p><a href="https://artilesphotography.com">artilesphotography.com</a></p>
				            %s
				        </div>
				    </div>
				</body>
				</html>
				"""
				.formatted(
						logoUrl,
						escapeHtml(message.getName()),
						escapeHtml(message.getService() != null ? message.getService() : "No especificado"),
						escapeHtml(message.getMessage()),
						message.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a")),
						socialMediaLinks);

		clientHelper.setText(clientHtmlContent, true);
		mailSender.send(clientMessage);
		logger.info("Correo de confirmación enviado exitosamente al cliente para el mensaje de contacto ID: {}",
				message.getId());
	}

	/**
	 * Sends a custom email to a specified recipient.
	 * 
	 * @param from    The sender's email address.
	 * @param to      The recipient's email address.
	 * @param subject The subject of the email.
	 * @param date    The date to display in the email.
	 * @param body    The body content of the email (plain text).
	 * @throws MessagingException if email sending fails.
	 */
	public void sendCustomEmail(String from, String to, String subject, String date, String body)
			throws MessagingException {
		// Obtener logo desde ConfigurationService
		String logoUrl = FALLBACK_LOGO_URL;
		try {
			ConfigurationResponse config = configurationService.getConfiguration();
			if (config != null && config.getLogoUrl() != null && !config.getLogoUrl().isBlank()) {
				logoUrl = config.getLogoUrl();
				logger.debug("Logo obtenido desde ConfigurationService: {}", logoUrl);
			} else {
				logger.warn("No se encontró un logo válido en la configuración. Usando logo por defecto: {}", logoUrl);
			}
		} catch (Exception e) {
			logger.error("Error al obtener el logo desde ConfigurationService: {}", e.getMessage(), e);
			logger.info("Usando logo por defecto debido al error: {}", logoUrl);
		}

		// Obtener información de contacto para redes sociales
		ContactInfoResponse contactInfo = contactInfoService.getContactInfo();
		String facebookUrl = validateUrl(contactInfo.getFacebook()) ? contactInfo.getFacebook() : "";
		String instagramUrl = validateUrl(contactInfo.getInstagram()) ? contactInfo.getInstagram() : "";
		String twitterUrl = validateUrl(contactInfo.getTwitter()) ? contactInfo.getTwitter() : "";
		String tiktokUrl = validateUrl(contactInfo.getTiktok()) ? contactInfo.getTiktok() : "";

		String socialMediaLinks = "";
		if (!facebookUrl.isEmpty() || !instagramUrl.isEmpty() || !twitterUrl.isEmpty() || !tiktokUrl.isEmpty()) {
			socialMediaLinks = "<div class='social-media' style='text-align: center; margin-top: 20px;'>";
			if (!facebookUrl.isEmpty()) {
				socialMediaLinks += String.format(
						"<a href='%s' style='margin: 0 10px;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/facebook.svg' alt='Facebook' width='24' height='24' style='vertical-align: middle;'></a>",
						facebookUrl);
			}
			if (!instagramUrl.isEmpty()) {
				socialMediaLinks += String.format(
						"<a href='%s' style='margin: 0 10px;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/instagram.svg' alt='Instagram' width='24' height='24' style='vertical-align: middle;'></a>",
						instagramUrl);
			}
			if (!twitterUrl.isEmpty()) {
				socialMediaLinks += String.format(
						"<a href='%s' style='margin: 0 10px;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/x.svg' alt='X' width='24' height='24' style='vertical-align: middle;'></a>",
						twitterUrl);
			}
			if (!tiktokUrl.isEmpty()) {
				socialMediaLinks += String.format(
						"<a href='%s' style='margin: 0 10px;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/tiktok.svg' alt='TikTok' width='24' height='24' style='vertical-align: middle;'></a>",
						tiktokUrl);
			}
			socialMediaLinks += "</div>";
		}

		// Enviar correo personalizado
		MimeMessage customMessage = mailSender.createMimeMessage();
		MimeMessageHelper customHelper = new MimeMessageHelper(customMessage, true, "UTF-8");

		customHelper.setTo(to);
		customHelper.setSubject(subject);
		customHelper.setFrom(from);

		// Cuerpo del correo personalizado
		String customHtmlContent = """
				<!DOCTYPE html>
				<html>
				<head>
				    <meta charset="UTF-8">
				    <meta name="viewport" content="width=device-width, initial-scale=1.0">
				    <style>
				        body { font-family: 'Helvetica Neue', Arial, sans-serif; color: #333; line-height: 1.6; margin: 0; padding: 0; }
				        .container { max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 12px; background-color: #f9f9f9; }
				        .header { background-color: #333333; color: white; padding: 15px; text-align: center; border-radius: 12px 12px 0 0; }
				        .header img { max-width: 150px; height: auto; }
				        .content { padding: 20px; background-color: white; border-radius: 0 0 12px 12px; }
				        .field { margin-bottom: 15px; }
				        .field-label { font-weight: bold; color: #444; display: flex; align-items: center; }
				        .field-label::before { content: '📷'; margin-right: 8px; font-size: 16px; }
				        .field-value { margin: 5px 0 0 24px; color: #555; }
				        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #777; }
				        .footer a { color: #d4a017; text-decoration: none; }
				        .footer a:hover { text-decoration: underline; }
				        @media screen and (max-width: 600px) {
				            .container { margin: 10px; padding: 15px; }
				            .header img { max-width: 120px; }
				            .content { padding: 15px; }
				        }
				    </style>
				</head>
				<body>
				    <div class="container">
				        <div class="header">
				            <img src="%s" alt="Artiles Photography Studio">
				            <h2>%s</h2>
				        </div>
				        <div class="content">
				            <div class="field">
				                <span class="field-label">Fecha:</span>
				                <p class="field-value">%s</p>
				            </div>
				            <div class="field">
				                <span class="field-label">Mensaje:</span>
				                <p class="field-value">%s</p>
				            </div>
				            <p>Si tienes alguna pregunta, no dudes en responder a este correo.</p>
				        </div>
				        <div class="footer">
				            <p>Artiles Photography Studio</p>
				            <p><a href="https://artilesphotography.com">artilesphotography.com</a></p>
				            %s
				        </div>
				    </div>
				</body>
				</html>
				"""
				.formatted(
						logoUrl,
						escapeHtml(subject),
						escapeHtml(date),
						escapeHtml(body),
						socialMediaLinks);

		customHelper.setText(customHtmlContent, true);
		mailSender.send(customMessage);
		logger.info("Correo personalizado enviado exitosamente a: {}", to);
	}

	// Método para validar URLs
	private boolean validateUrl(String url) {
		return url != null && !url.trim().isEmpty() && (url.startsWith("http://") || url.startsWith("https://"));
	}

	// Método para escapar caracteres HTML y prevenir XSS
	private String escapeHtml(String input) {
		if (input == null)
			return "";
		return input.replace("&", "&")
				.replace("<", "<")
				.replace(">", ">")
				.replace("\"", "\"")
				.replace("'", "'");
	}
}