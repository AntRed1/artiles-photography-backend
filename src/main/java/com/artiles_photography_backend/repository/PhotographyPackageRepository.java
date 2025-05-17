package com.artiles_photography_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.artiles_photography_backend.models.PhotographyPackage;

/**
 *
 * @author arojas
 *         Repositorio para operaciones CRUD sobre la entidad
 *         PhotographyPackage.
 *
 */
@Repository
public interface PhotographyPackageRepository extends JpaRepository<PhotographyPackage, Long> {
	List<PhotographyPackage> findByIsActiveTrue();
}