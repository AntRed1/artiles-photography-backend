package com.artiles_photography_backend.services;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artiles_photography_backend.repository.JwtBlacklistRepository;

/**
 * @author arojas
 *         Servicio para limpiar tokens JWT expirados de la lista negra.
 */
@Service
public class JwtBlacklistCleanupService {

	private static final Logger logger = LoggerFactory.getLogger(JwtBlacklistCleanupService.class);

	private final JwtBlacklistRepository jwtBlacklistRepository;

	@Autowired
	public JwtBlacklistCleanupService(JwtBlacklistRepository jwtBlacklistRepository) {
		this.jwtBlacklistRepository = jwtBlacklistRepository;
	}

	@Scheduled(cron = "0 0 0 * * ?") // Ejecutar todos los días a medianoche
	@Transactional
	public void cleanupExpiredTokens() {
		try {
			LocalDateTime now = LocalDateTime.now();
			int deletedCount = jwtBlacklistRepository.deleteByExpiryDateBefore(now);
			logger.info("Limpieza de tokens expirados completada. Tokens eliminados: {}", deletedCount);
		} catch (Exception e) {
			logger.error("Error al limpiar tokens expirados: {}", e.getMessage(), e);
		}
	}
}