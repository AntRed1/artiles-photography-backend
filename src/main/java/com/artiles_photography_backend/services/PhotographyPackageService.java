package com.artiles_photography_backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.PhotographyPackage;
import com.artiles_photography_backend.repository.PhotographyPackageRepository;

/**
 *
 * @author arojas
 *         Servicio para manejar operaciones relacionadas con
 *         PhotographyPackage.
 *
 */
@Service
public class PhotographyPackageService {

	private final PhotographyPackageRepository photographyPackageRepository;

	@Autowired
	public PhotographyPackageService(PhotographyPackageRepository photographyPackageRepository) {
		this.photographyPackageRepository = photographyPackageRepository;
	}

	/**
	 * Obtiene todos los paquetes fotográficos.
	 */
	public List<PhotographyPackage> getAllPhotographyPackages() {
		return (List<PhotographyPackage>) photographyPackageRepository.findAll();
	}

	/**
	 * Obtiene los paquetes fotográficos activos.
	 */
	public List<PhotographyPackage> getActivePhotographyPackages() {
		return photographyPackageRepository.findByIsActiveTrue();
	}

	/**
	 * Guarda un paquete fotográfico en la base de datos.
	 */
	public PhotographyPackage savePhotographyPackage(PhotographyPackage photographyPackage) {
		return photographyPackageRepository.save(photographyPackage);
	}
}
