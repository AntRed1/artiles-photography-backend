package com.artiles_photography_backend.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
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
public interface PhotographyPackageRepository extends CrudRepository<PhotographyPackage, Long> {
	// Método para obtener solo los paquetes activos
	List<PhotographyPackage> findByIsActiveTrue();
}
