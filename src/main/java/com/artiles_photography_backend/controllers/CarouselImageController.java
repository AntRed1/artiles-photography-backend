package com.artiles_photography_backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.models.CarouselImage;
import com.artiles_photography_backend.services.CarouselImageService;

/**
 *
 * @author arojas
 */
@RestController
@RequestMapping("/api/carousel")
public class CarouselImageController {
	@Autowired
	private CarouselImageService carouselImageService;

	@GetMapping
	public List<CarouselImage> getAllCarouselImages() {
		return carouselImageService.getAllCarouselImages();
	}
}
