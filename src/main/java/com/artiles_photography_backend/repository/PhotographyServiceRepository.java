package com.artiles_photography_backend.repository;

import org.springframework.data.repository.CrudRepository;
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
public interface PhotographyServiceRepository extends CrudRepository<PhotographyService, Long> {
}
