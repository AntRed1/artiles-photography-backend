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

import com.artiles_photography_backend.dtos.AboutUsRequest;
import com.artiles_photography_backend.dtos.AboutUsResponse;
import com.artiles_photography_backend.services.AboutUsService;

import jakarta.validation.Valid;

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

	public AboutUsController(AboutUsService aboutUsService) {
		this.aboutUsService = aboutUsService;
	}

	@GetMapping
	public ResponseEntity<?> getAboutUs() {
		AboutUsResponse aboutUs = aboutUsService.getAboutUs();
		if (aboutUs == null) {
			return ResponseEntity.status(204).body("No se encontró información.");
		}
		return ResponseEntity.ok(aboutUs);
	}

	@GetMapping("/{id}")
	public ResponseEntity<AboutUsResponse> getAboutUsById(@PathVariable Long id) {
		return ResponseEntity.ok(aboutUsService.getAboutUsById(id));
	}

	@GetMapping("/by-title/{title}")
	public ResponseEntity<?> getAboutUsByTitle(@PathVariable String title) {
		AboutUsResponse aboutUs = aboutUsService.getAboutUsByTitle(title);
		if (aboutUs == null) {
			return ResponseEntity.status(404).body("No se encontró información para el título: " + title);
		}
		return ResponseEntity.ok(aboutUs);
	}

	@GetMapping("/all")
	public ResponseEntity<List<AboutUsResponse>> getAllAboutUs() {
		return ResponseEntity.ok(aboutUsService.getAllAboutUs());
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AboutUsResponse> createAboutUs(@Valid @RequestBody AboutUsRequest request) {
		return ResponseEntity.status(201).body(aboutUsService.createAboutUs(request));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AboutUsResponse> updateAboutUs(@PathVariable Long id,
			@Valid @RequestBody AboutUsRequest request) {
		return ResponseEntity.ok(aboutUsService.updateAboutUs(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteAboutUs(@PathVariable Long id) {
		aboutUsService.deleteAboutUs(id);
		return ResponseEntity.noContent().build();
	}
}