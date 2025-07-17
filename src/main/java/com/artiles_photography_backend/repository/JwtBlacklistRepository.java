package com.artiles_photography_backend.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.artiles_photography_backend.models.JwtBlacklist;

import jakarta.transaction.Transactional;

/**
 * @author arojas
 *         Repositorio para gestionar tokens JWT en lista negra.
 */
@Transactional
@Repository
public interface JwtBlacklistRepository extends JpaRepository<JwtBlacklist, Long> {
	boolean existsByTokenHash(String tokenHash);

	int deleteByExpiryDateBefore(LocalDateTime expiryDate);
}
