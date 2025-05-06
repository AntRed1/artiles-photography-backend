package com.artiles_photography_backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.Testimonial;
import com.artiles_photography_backend.repository.TestimonialRepository;

/**
 *
 * @author arojas
 *         * Servicio para manejar operaciones relacionadas con Testimonial.
 * 
 */
@Service
public class TestimonialService {

    private final TestimonialRepository testimonialRepository;

    @Autowired
    public TestimonialService(TestimonialRepository testimonialRepository) {
        this.testimonialRepository = testimonialRepository;
    }

    /**
     * Obtiene todos los testimonios.
     */
    public List<Testimonial> getAllTestimonials() {
        return (List<Testimonial>) testimonialRepository.findAll();
    }

    /**
     * Guarda un testimonio.
     */
    public Testimonial saveTestimonial(Testimonial testimonial) {
        return testimonialRepository.save(testimonial);
    }
}
