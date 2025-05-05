package com.artiles_photography_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.artiles_photography_backend.models.Legal;

/**
 *
 * @author arojas
 */
@Repository
public interface LegalRepository extends JpaRepository<Legal, Long> {
    List<Legal> findByType(String type);
}
