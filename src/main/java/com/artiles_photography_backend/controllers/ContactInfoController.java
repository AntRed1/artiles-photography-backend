package com.artiles_photography_backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.models.ContactInfo;
import com.artiles_photography_backend.services.ContactInfoService;

/**
 *
 * @author arojas
 *         * Controlador REST para manejar peticiones relacionadas con
 *         ContactInfo.
 * 
 */
@RestController
@RequestMapping("/api/contact-info")
public class ContactInfoController {

	private final ContactInfoService contactInfoService;

	@Autowired
	public ContactInfoController(ContactInfoService contactInfoService) {
		this.contactInfoService = contactInfoService;
	}

	/**
	 * Obtiene la información de contacto.
	 */
	@GetMapping
	public ResponseEntity<ContactInfo> getContactInfo() {
		return ResponseEntity.ok(contactInfoService.getContactInfo());
	}

	/**
	 * Guarda o actualiza la información de contacto.
	 */
	@PostMapping
	public ResponseEntity<ContactInfo> saveContactInfo(@RequestBody ContactInfo contactInfo) {
		return ResponseEntity.ok(contactInfoService.saveContactInfo(contactInfo));
	}
}
