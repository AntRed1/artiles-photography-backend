package com.artiles_photography_backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	@GetMapping
	public ResponseEntity<?> getAboutUs() {
		AboutUs aboutUs = aboutUsService.getAboutUs();
		if (aboutUs == null) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body("No se encontró información.");
		}
		return ResponseEntity.ok(aboutUs);
	}

	@GetMapping("/by-title/{title}")
	public ResponseEntity<?> getAboutUsByTitle(@PathVariable String title) {
		AboutUs aboutUs = aboutUsService.getAboutUsByTitle(title);
		if (aboutUs == null) {
			System.out.println("No se encontró la información para el título: " + title);
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("No se encontró información para el título: " + title);
		}
		return ResponseEntity.ok(aboutUs);
	}

	@PostMapping
	public ResponseEntity<AboutUs> saveAboutUs(@RequestBody AboutUs aboutUs) {
		return ResponseEntity.ok(aboutUsService.saveAboutUs(aboutUs));
	}
}