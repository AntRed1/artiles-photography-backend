package com.artiles_photography_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.artiles_photography_backend.models.AboutUs;

/**
 *
 * @author arojas
 */
@Repository
public interface AboutUsRepository extends JpaRepository<AboutUs, Long> {
	Optional<AboutUs> findByTitle(String title);

	Optional<AboutUs> findFirstByOrderByIdAsc();
}