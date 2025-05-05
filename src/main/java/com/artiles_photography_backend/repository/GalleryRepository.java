package com.artiles_photography_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.artiles_photography_backend.models.Gallery;

/**
 *
 * @author arojas
 */
@Repository
public interface GalleryRepository extends JpaRepository<Gallery, Long> {
	List<Gallery> findByDescriptionContaining(String type); // e.g., 'carousel' or 'gallery'
}