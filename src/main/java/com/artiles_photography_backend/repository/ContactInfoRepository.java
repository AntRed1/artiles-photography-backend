package com.artiles_photography_backend.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.artiles_photography_backend.models.ContactInfo;

/**
 *
 * @author arojas
 *         * Repositorio para operaciones CRUD sobre la entidad ContactInfo.
 * 
 */
@Repository
public interface ContactInfoRepository extends CrudRepository<ContactInfo, Long> {
}