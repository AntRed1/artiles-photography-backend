package com.artiles_photography_backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.models.PhotographyService;
import com.artiles_photography_backend.services.PhotographyServiceService;

/**
 *
 * @author arojas
 *         * Controlador REST para manejar peticiones relacionadas con
 *         PhotographyService.
 * 
 */
@RestController
@RequestMapping("/api/services")
public class PhotographyServiceController {

	private final PhotographyServiceService photographyServiceService;

	@Autowired
	public PhotographyServiceController(PhotographyServiceService photographyServiceService) {
		this.photographyServiceService = photographyServiceService;
	}

	/**
	 * Obtiene todos los servicios fotográficos.
	 */
	@GetMapping
	public ResponseEntity<List<PhotographyService>> getAllPhotographyServices() {
		return ResponseEntity.ok(photographyServiceService.getAllPhotographyServices());
	}

	/**
	 * Guarda un nuevo servicio fotográfico o actualiza uno existente.
	 */
	@PostMapping
	public ResponseEntity<PhotographyService> savePhotographyService(
			@RequestBody PhotographyService photographyService) {
		return ResponseEntity.ok(photographyServiceService.savePhotographyService(photographyService));
	}
}
