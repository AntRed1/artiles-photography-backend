package com.artiles_photography_backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.models.PhotographyPackage;
import com.artiles_photography_backend.services.PhotographyPackageService;

/**
 *
 * @author arojas
 *         Controlador REST para manejar peticiones relacionadas con
 *         PhotographyPackage.
 * 
 */
@RestController
@RequestMapping("/api/packages")
public class PhotographyPackageController {

	private final PhotographyPackageService photographyPackageService;

	@Autowired
	public PhotographyPackageController(PhotographyPackageService photographyPackageService) {
		this.photographyPackageService = photographyPackageService;
	}

	/**
	 * Obtiene todos los paquetes fotográficos.
	 */
	@GetMapping
	public ResponseEntity<List<PhotographyPackage>> getAllPhotographyPackages() {
		return ResponseEntity.ok(photographyPackageService.getAllPhotographyPackages());
	}

	/**
	 * Obtiene los paquetes fotográficos activos.
	 */
	@GetMapping("/active")
	public ResponseEntity<List<PhotographyPackage>> getActivePhotographyPackages() {
		return ResponseEntity.ok(photographyPackageService.getActivePhotographyPackages());
	}

	/**
	 * Guarda un nuevo paquete fotográfico o actualiza uno existente.
	 */
	@PostMapping
	public ResponseEntity<PhotographyPackage> savePhotographyPackage(
			@RequestBody PhotographyPackage photographyPackage) {
		return ResponseEntity.ok(photographyPackageService.savePhotographyPackage(photographyPackage));
	}
}
