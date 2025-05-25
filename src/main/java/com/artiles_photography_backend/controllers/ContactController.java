package com.artiles_photography_backend.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.dtos.ContactMessageRequest;
import com.artiles_photography_backend.dtos.ContactMessageResponse;
import com.artiles_photography_backend.models.ContactMessage;
import com.artiles_photography_backend.services.ContactMessageService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * @author arojas
 *         Controlador REST para manejar peticiones relacionadas con
 *         ContactMessage.
 */
@RestController
@RequestMapping("/api/contact")
public class ContactController {

	private final ContactMessageService contactMessageService;

	public ContactController(ContactMessageService contactMessageService) {
		this.contactMessageService = contactMessageService;
	}

	/**
	 * Recibe un nuevo mensaje de contacto.
	 * Acceso público.
	 */
	@PostMapping
	public ResponseEntity<Map<String, String>> submitContactForm(
			@Valid @RequestBody ContactMessageRequest request,
			HttpServletRequest httpRequest) {

		String clientIp = getClientIp(httpRequest);
		String userAgent = httpRequest.getHeader("User-Agent");

		contactMessageService.createContactMessage(request, clientIp, userAgent);

		Map<String, String> response = new HashMap<>();
		response.put("message", "Mensaje recibido con éxito. Te contactaremos pronto.");
		return ResponseEntity.status(201).body(response);
	}

	private String getClientIp(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
		return (xfHeader == null || xfHeader.isEmpty()) ? request.getRemoteAddr() : xfHeader;
	}

	// ------------------- Admin Endpoints -------------------

	@GetMapping("/admin/messages")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<ContactMessageResponse>> getAllMessages() {
		return ResponseEntity.ok(contactMessageService.getAllContactMessages());
	}

	@GetMapping("/admin/messages/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ContactMessageResponse> getMessageById(@PathVariable Long id) {
		return ResponseEntity.ok(contactMessageService.getContactMessageById(id));
	}

	@GetMapping("/admin/messages/email/{email}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<ContactMessageResponse>> getMessagesByEmail(@PathVariable String email) {
		return ResponseEntity.ok(contactMessageService.getContactMessagesByEmail(email));
	}

	@GetMapping("/admin/messages/service/{service}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<ContactMessageResponse>> getMessagesByService(@PathVariable String service) {
		return ResponseEntity.ok(contactMessageService.getContactMessagesByService(service));
	}

	@PutMapping("/admin/messages/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ContactMessageResponse> updateMessage(
			@PathVariable Long id,
			@Valid @RequestBody ContactMessageRequest request) {
		return ResponseEntity.ok(contactMessageService.updateContactMessage(id, request));
	}

	@DeleteMapping("/admin/messages/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
		contactMessageService.deleteContactMessage(id);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Sends an email, either resending the confirmation email for a specific
	 * message or a custom email.
	 * 
	 * @param request The email request containing either a messageId for resending
	 *                or custom email fields.
	 * @return A response indicating success.
	 * @throws MessagingException if email sending fails.
	 */
	@PostMapping("/admin/messages/send-email")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, String>> sendEmail(@Valid @RequestBody EmailRequest request)
			throws MessagingException {
		if (request.getMessageId() != null) {
			// Resend confirmation email for a specific message
			ContactMessageResponse message = contactMessageService.getContactMessageById(request.getMessageId());
			ContactMessage contactMessage = new ContactMessage();
			contactMessage.setId(message.getId());
			contactMessage.setName(message.getName());
			contactMessage.setEmail(message.getEmail());
			contactMessage.setPhone(message.getPhone());
			contactMessage.setService(message.getService());
			contactMessage.setMessage(message.getMessage());
			contactMessage.setClientIp(message.getClientIp());
			contactMessage.setUserAgent(message.getUserAgent());
			contactMessage.setCreatedAt(message.getCreatedAt());
			contactMessageService.sendContactEmail(contactMessage);
		} else if (request.getFrom() != null && request.getTo() != null && request.getSubject() != null
				&& request.getDate() != null && request.getBody() != null) {
			// Send a custom email
			contactMessageService.sendCustomEmail(
					request.getFrom(),
					request.getTo(),
					request.getSubject(),
					request.getDate(),
					request.getBody());
		} else {
			throw new IllegalArgumentException(
					"Must provide either messageId for resending or all fields for a custom email.");
		}

		Map<String, String> response = new HashMap<>();
		response.put("message", "Correo enviado exitosamente.");
		return ResponseEntity.ok(response);
	}

	/**
	 * DTO for email sending requests.
	 */
	public static class EmailRequest {
		private Long messageId; // For resending confirmation email
		private String from; // For custom email
		private String to; // For custom email
		private String subject; // For custom email
		private String date; // For custom email
		private String body; // For custom email

		// Getters and setters
		public Long getMessageId() {
			return messageId;
		}

		public void setMessageId(Long messageId) {
			this.messageId = messageId;
		}

		public String getFrom() {
			return from;
		}

		public void setFrom(String from) {
			this.from = from;
		}

		public String getTo() {
			return to;
		}

		public void setTo(String to) {
			this.to = to;
		}

		public String getSubject() {
			return subject;
		}

		public void setSubject(String subject) {
			this.subject = subject;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getBody() {
			return body;
		}

		public void setBody(String body) {
			this.body = body;
		}
	}
}