package com.artiles_photography_backend.controllers;

import java.util.List;

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
 *         * Controlador REST para manejar peticiones relacionadas con AboutUs.
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
	 * Obtiene toda la información de AboutUs.
	 */
	@GetMapping
	public ResponseEntity<List<AboutUs>> getAllAboutUs() {
		return ResponseEntity.ok(aboutUsService.getAllAboutUs());
	}

	/**
	 * Guarda o actualiza la información de AboutUs.
	 */
	@PostMapping
	public ResponseEntity<AboutUs> saveAboutUs(@RequestBody AboutUs aboutUs) {
		return ResponseEntity.ok(aboutUsService.saveAboutUs(aboutUs));
	}
}