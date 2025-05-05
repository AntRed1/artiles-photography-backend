package com.artiles_photography_backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.PhotographyService;
import com.artiles_photography_backend.repository.PhotographyServiceRepository;

/**
 *
 * @author arojas
 *         * Servicio para manejar operaciones relacionadas con
 *         PhotographyService.
 * 
 */
@Service
public class PhotographyServiceService {

	private final PhotographyServiceRepository photographyServiceRepository;

	@Autowired
	public PhotographyServiceService(PhotographyServiceRepository photographyServiceRepository) {
		this.photographyServiceRepository = photographyServiceRepository;
	}

	/**
	 * Obtiene todos los servicios fotográficos.
	 */
	public List<PhotographyService> getAllPhotographyServices() {
		return (List<PhotographyService>) photographyServiceRepository.findAll();
	}

	/**
	 * Guarda un servicio fotográfico en la base de datos.
	 */
	public PhotographyService savePhotographyService(PhotographyService photographyService) {
		return photographyServiceRepository.save(photographyService);
	}
}
