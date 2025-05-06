package com.artiles_photography_backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.models.ContactMessage;
import com.artiles_photography_backend.repository.ContactMessageRepository;

import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author arojas
 */
@RestController
@RequestMapping("/api/contact")
public class ContactController {

	@Autowired
	private ContactMessageRepository repository;

	@PostMapping
	public ResponseEntity<String> submitContactForm(
			@RequestBody ContactFormDTO form,
			HttpServletRequest request) {
		ContactMessage message = new ContactMessage();
		message.setName(form.getName());
		message.setEmail(form.getEmail());
		message.setPhone(form.getPhone());
		message.setService(form.getService());
		message.setMessage(form.getMessage());
		message.setClientIp(request.getRemoteAddr());
		message.setUserAgent(request.getHeader("User-Agent"));

		repository.save(message);
		return ResponseEntity.ok("Mensaje recibido con éxito");
	}

	public static class ContactFormDTO {
		private String name;
		private String email;
		private String phone;
		private String service;
		private String message;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public String getService() {
			return service;
		}

		public void setService(String service) {
			this.service = service;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
}