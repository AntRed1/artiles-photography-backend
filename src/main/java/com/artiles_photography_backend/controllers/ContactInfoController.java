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

import com.artiles_photography_backend.dtos.ContactInfoRequest;
import com.artiles_photography_backend.dtos.ContactInfoResponse;
import com.artiles_photography_backend.services.ContactInfoService;

import jakarta.validation.Valid;

/**
 * @author arojas
 *         Controlador REST para manejar peticiones relacionadas con
 *         ContactInfo.
 */
@RestController
@RequestMapping("/api/contact-info")
public class ContactInfoController {

	private final ContactInfoService contactInfoService;

	public ContactInfoController(ContactInfoService contactInfoService) {
		this.contactInfoService = contactInfoService;
	}

	@GetMapping
	public ResponseEntity<?> getContactInfo() {
		ContactInfoResponse contactInfo = contactInfoService.getContactInfo();
		if (contactInfo == null) {
			return ResponseEntity.status(404).body("Información de contacto no encontrada.");
		}
		return ResponseEntity.ok(contactInfo);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ContactInfoResponse> getContactInfoById(@PathVariable Long id) {
		return ResponseEntity.ok(contactInfoService.getContactInfoById(id));
	}

	@GetMapping("/all")
	public ResponseEntity<List<ContactInfoResponse>> getAllContactInfo() {
		return ResponseEntity.ok(contactInfoService.getAllContactInfo());
	}

	@PostMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ContactInfoResponse> createContactInfo(@Valid @RequestBody ContactInfoRequest request) {
		return ResponseEntity.status(201).body(contactInfoService.createContactInfo(request));
	}

	@PutMapping("/admin/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ContactInfoResponse> updateContactInfo(@PathVariable Long id,
			@Valid @RequestBody ContactInfoRequest request) {
		return ResponseEntity.ok(contactInfoService.updateContactInfo(id, request));
	}

	@DeleteMapping("/admin/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteContactInfo(@PathVariable Long id) {
		contactInfoService.deleteContactInfo(id);
		return ResponseEntity.noContent().build();
	}
}