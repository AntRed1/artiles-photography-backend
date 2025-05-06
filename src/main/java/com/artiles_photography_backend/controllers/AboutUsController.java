package com.artiles_photography_backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.models.AboutUs;
import com.artiles_photography_backend.services.AboutUsService;

/**
 *
 * @author arojas
 *         Controlador REST para manejar peticiones relacionadas con AboutUs.
 * 
 */

@RestController
@RequestMapping("/api/information")
public class AboutUsController {

	private final AboutUsService aboutUsService;

	@Autowired
	public AboutUsController(AboutUsService aboutUsService) {
		this.aboutUsService = aboutUsService;
	}

	/**
	 * Obtiene la información "Sobre Nosotros".
	 */
	@GetMapping
	public ResponseEntity<AboutUs> getAboutUs() {
		AboutUs aboutUs = aboutUsService.getAboutUs();
		if (aboutUs == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(aboutUs);
	}

	/**
	 * Guarda la información "Sobre Nosotros".
	 */
	@PostMapping
	public ResponseEntity<AboutUs> saveAboutUs(@RequestBody AboutUs aboutUs) {
		return ResponseEntity.ok(aboutUsService.saveAboutUs(aboutUs));
	}
}