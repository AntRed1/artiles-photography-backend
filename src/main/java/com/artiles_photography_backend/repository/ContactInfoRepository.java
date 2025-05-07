package com.artiles_photography_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.artiles_photography_backend.models.ContactInfo;

/**
 *
 * @author arojas
 *         * Repositorio para operaciones CRUD sobre la entidad ContactInfo.
 * 
 */
@Repository
public interface ContactInfoRepository extends JpaRepository<ContactInfo, Long> {
}