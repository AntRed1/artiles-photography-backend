package com.artiles_photography_backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.CarouselImage;
import com.artiles_photography_backend.repository.CarouselImageRepository;

/**
 *
 * @author arojas
 */
@Service
public class CarouselImageService {
    @Autowired
    private CarouselImageRepository carouselImageRepository;

    public List<CarouselImage> getAllCarouselImages() {
        return carouselImageRepository.findAllByOrderByDisplayOrderAsc();
    }

    public CarouselImage saveCarouselImage(CarouselImage carouselImage) {
        return carouselImageRepository.save(carouselImage);
    }
}
