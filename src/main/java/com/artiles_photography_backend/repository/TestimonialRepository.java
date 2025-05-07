package com.artiles_photography_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.artiles_photography_backend.models.Testimonial;

/**
 *
 * @author arojas
 *         * Repositorio para operaciones CRUD sobre la entidad Testimonial.
 *
 */
@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {
	List<Testimonial> findByEnableTrue();
}