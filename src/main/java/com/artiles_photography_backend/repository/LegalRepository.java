package com.artiles_photography_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.artiles_photography_backend.models.Legal;

/**
 *
 * @author arojas
 *         * Repositorio para operaciones CRUD sobre la entidad Legal.
 * 
 */
@Repository
public interface LegalRepository extends JpaRepository<Legal, Long> {
	Optional<Legal> findByType(String type);
}