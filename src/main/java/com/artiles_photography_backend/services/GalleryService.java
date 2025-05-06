package com.artiles_photography_backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.Gallery;
import com.artiles_photography_backend.repository.GalleryRepository;

/**
 *
 * @author arojas
 *         * Servicio para manejar operaciones relacionadas con Gallery.
 * 
 */
@Service
public class GalleryService {

	private final GalleryRepository galleryRepository;

	@Autowired
	public GalleryService(GalleryRepository galleryRepository) {
		this.galleryRepository = galleryRepository;
	}

	/**
	 * Obtiene todas las imágenes de la galería.
	 */
	public List<Gallery> getAllGalleryImages() {
		return (List<Gallery>) galleryRepository.findAll();
	}

	/**
	 * Guarda una imagen en la galería.
	 */
	public Gallery saveGallery(Gallery gallery) {
		return galleryRepository.save(gallery);
	}
}