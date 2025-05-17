package com.artiles_photography_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.artiles_photography_backend.models.PhotographyService;

/**
 *
 * @author arojas
 *         * Repositorio para operaciones CRUD sobre la entidad
 *         PhotographyService.
 *
 */
@Repository
public interface PhotographyServiceRepository extends JpaRepository<PhotographyService, Long> {
	Optional<PhotographyService> findByTitle(String title);
}
