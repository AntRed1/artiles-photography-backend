package com.artiles_photography_backend.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.artiles_photography_backend.models.Testimonial;

/**
 *
 * @author arojas
 *         * Repositorio para operaciones CRUD sobre la entidad Testimonial.
 * 
 */
@Repository
public interface TestimonialRepository extends CrudRepository<Testimonial, Long> {
}
