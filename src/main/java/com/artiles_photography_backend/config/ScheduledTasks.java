package com.artiles_photography_backend.config;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.artiles_photography_backend.repository.JwtBlacklistRepository;

/**
 * @author arojas
 *         Clase para manejar tareas programadas, como la limpieza de tokens JWT
 *         expirados.
 */
@Component
public class ScheduledTasks {

	private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

	private final JwtBlacklistRepository jwtBlacklistRepository;

	@Autowired
	public ScheduledTasks(JwtBlacklistRepository jwtBlacklistRepository) {
		this.jwtBlacklistRepository = jwtBlacklistRepository;
	}

	@Scheduled(cron = "0 0 0 * * ?") // Ejecuta diariamente a medianoche
	@Transactional
	public void cleanExpiredTokens() {
		logger.info("Iniciando limpieza de tokens JWT expirados...");
		try {
			int deletedCount = jwtBlacklistRepository.deleteByExpiryDateBefore(LocalDateTime.now());
			logger.info("Limpieza completada. Tokens eliminados: {}", deletedCount);
		} catch (Exception e) {
			logger.error("Error al limpiar tokens JWT expirados: {}", e.getMessage(), e);
		}
	}
}