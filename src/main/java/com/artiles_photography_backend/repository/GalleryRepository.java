package com.artiles_photography_backend.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.artiles_photography_backend.models.Gallery;

/**
 *
 * @author arojas
 *         * Repositorio para operaciones CRUD sobre la entidad Gallery.
 * 
 */
@Repository
public interface GalleryRepository extends CrudRepository<Gallery, Long> {
}