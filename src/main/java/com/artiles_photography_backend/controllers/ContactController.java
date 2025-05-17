package com.artiles_photography_backend.controllers;

import java.util.List;

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
import com.artiles_photography_backend.services.ContactMessageService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 *
 * @author arojas
 */
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

	@PostMapping
	public ResponseEntity<String> submitContactForm(
			@Valid @RequestBody ContactMessageRequest request,
			HttpServletRequest httpRequest) {
		String clientIp = httpRequest.getHeader("X-Forwarded-For");
		if (clientIp == null || clientIp.isEmpty()) {
			clientIp = httpRequest.getRemoteAddr();
		}
		String userAgent = httpRequest.getHeader("User-Agent");
		contactMessageService.createContactMessage(request, clientIp, userAgent);
		return ResponseEntity.status(201).body("Mensaje recibido con éxito");
	}

	@GetMapping("/admin/contact-messages")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<ContactMessageResponse>> getAllContactMessages() {
		return ResponseEntity.ok(contactMessageService.getAllContactMessages());
	}

	@GetMapping("/admin/contact-messages/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ContactMessageResponse> getContactMessageById(@PathVariable Long id) {
		return ResponseEntity.ok(contactMessageService.getContactMessageById(id));
	}

	@GetMapping("/admin/contact-messages/email/{email}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<ContactMessageResponse>> getContactMessagesByEmail(@PathVariable String email) {
		return ResponseEntity.ok(contactMessageService.getContactMessagesByEmail(email));
	}

	@GetMapping("/admin/contact-messages/service/{service}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<ContactMessageResponse>> getContactMessagesByService(@PathVariable String service) {
		return ResponseEntity.ok(contactMessageService.getContactMessagesByService(service));
	}

	@PutMapping("/admin/contact-messages/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ContactMessageResponse> updateContactMessage(
			@PathVariable Long id,
			@Valid @RequestBody ContactMessageRequest request) {
		return ResponseEntity.ok(contactMessageService.updateContactMessage(id, request));
	}

	@DeleteMapping("/admin/contact-messages/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteContactMessage(@PathVariable Long id) {
		contactMessageService.deleteContactMessage(id);
		return ResponseEntity.noContent().build();
	}
}