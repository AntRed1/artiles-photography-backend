package com.artiles_photography_backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.Testimonial;
import com.artiles_photography_backend.repository.TestimonialRepository;

import jakarta.transaction.Transactional;

/**
 *
 * @author arojas
 *         * Servicio para manejar operaciones relacionadas con Testimonial.
 *
 */
@Service
@Transactional
public class TestimonialService {

    private final TestimonialRepository testimonialRepository;

    @Autowired
    public TestimonialService(TestimonialRepository testimonialRepository) {
        this.testimonialRepository = testimonialRepository;
    }

    /**
     * Obtiene todos los testimonios habilitados.
     */
    public List<Testimonial> getAllTestimonials() {
        return testimonialRepository.findByEnableTrue();
    }

    /**
     * Guarda un testimonio.
     */
    public Testimonial saveTestimonial(Testimonial testimonial) {
        if (testimonial.getId() != null) {
            throw new IllegalArgumentException("El ID del testimonio debe ser nulo para nuevos registros");
        }
        return testimonialRepository.save(testimonial);
    }
}
