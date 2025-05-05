package com.artiles_photography_backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.Gallery;
import com.artiles_photography_backend.repository.GalleryRepository;

/**
 *
 * @author arojas
 */
@Service
public class GalleryService {
	@Autowired
	private GalleryRepository galleryRepository;

	public List<Gallery> getCarouselImages() {
		return galleryRepository.findByDescriptionContaining("carousel");
	}

	public List<Gallery> getGalleryImages() {
		return galleryRepository.findByDescriptionContaining("gallery");
	}

	public Gallery saveGalleryImage(Gallery gallery) {
		return galleryRepository.save(gallery);
	}
}