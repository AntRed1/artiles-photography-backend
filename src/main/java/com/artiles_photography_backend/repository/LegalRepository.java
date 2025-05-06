package com.artiles_photography_backend.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.artiles_photography_backend.models.Legal;

/**
 *
 * @author arojas
 *         * Repositorio para operaciones CRUD sobre la entidad Legal.
 * 
 */
@Repository
public interface LegalRepository extends CrudRepository<Legal, Long> {
}