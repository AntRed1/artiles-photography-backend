package com.artiles_photography_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.artiles_photography_backend.models.Configuration;

/**
 *
 * @author arojas
 */
@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {
}
