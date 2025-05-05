package com.artiles_photography_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.artiles_photography_backend.models.AboutUs;

/**
 *
 * @author arojas
 */
@Repository
public interface AboutUsRepository extends JpaRepository<AboutUs, Long> {

	AboutUs findByTitle(String title);
}