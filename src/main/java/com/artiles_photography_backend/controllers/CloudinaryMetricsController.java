package com.artiles_photography_backend.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * @author arojas
 *         Controlador para gestionar métricas y operaciones de Cloudinary.
 *         Incluye obtener métricas de uso, listar imágenes y eliminar imágenes.
 */
@RestController
@RequestMapping("/api/cloudinary-metrics")
public class CloudinaryMetricsController {

	private static final Logger logger = LoggerFactory.getLogger(CloudinaryMetricsController.class);

	@Autowired
	private Cloudinary cloudinary;

	// Caché para métricas con TTL de 5 minutos
	private final Cache<String, Map<String, Object>> metricsCache = Caffeine.newBuilder()
			.expireAfterWrite(5, TimeUnit.MINUTES)
			.maximumSize(100)
			.build();

	// Caché para next_cursor con TTL de 10 minutos
	private final Cache<String, String> cursorCache = Caffeine.newBuilder()
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.maximumSize(1000)
			.build();

	@GetMapping
	public ResponseEntity<Map<String, Object>> getCloudinaryMetrics() {
		String cacheKey = "cloudinary_metrics";
		Map<String, Object> cachedMetrics = metricsCache.getIfPresent(cacheKey);
		if (cachedMetrics != null) {
			logger.info("Métricas de Cloudinary servidas desde caché");
			return ResponseEntity.ok(cachedMetrics);
		}

		Map<String, Object> metrics = new HashMap<>();
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		try {
			// Total de imágenes
			Map<String, Object> resourceOptions = ObjectUtils.asMap(
					"resource_type", "image",
					"max_results", 1);
			Map<String, Object> resourcesResponse = cloudinary.api().resources(resourceOptions);
			Number totalResources = (Number) resourcesResponse.get("total_count");
			metrics.put("totalImages", totalResources != null ? totalResources.longValue() : 0L);

			// Uso de almacenamiento y transformaciones (current)
			Map<String, Object> usageResponse = cloudinary.api().usage(ObjectUtils.emptyMap());
			Map<String, Object> objects = (Map<String, Object>) usageResponse.get("objects");
			Map<String, Object> transformations = (Map<String, Object>) usageResponse.get("transformations");

			long storageUsageBytes = objects != null && objects.get("used") instanceof Number
					? ((Number) objects.get("used")).longValue()
					: 0L;
			long transformationCount = transformations != null && transformations.get("used") instanceof Number
					? ((Number) transformations.get("used")).longValue()
					: 0L;

			metrics.put("storageUsageBytes", storageUsageBytes);
			metrics.put("transformationCount", transformationCount);

			// Subidas recientes
			Map<String, Object> recentOptions = ObjectUtils.asMap(
					"resource_type", "image",
					"max_results", 10,
					"direction", "desc");
			Map<String, Object> recentUploadsResponse = cloudinary.api().resources(recentOptions);
			List<Map<String, Object>> recentUploads = (List<Map<String, Object>>) recentUploadsResponse
					.get("resources");
			metrics.put("recentUploads", recentUploads);

			// Historical trend data (limitado a 5 días para reducir operaciones)
			List<Map<String, Object>> trendData = new ArrayList<>();
			LocalDate endDate = LocalDate.now().minusDays(1); // Yesterday
			int daysToFetch = 5;

			for (int i = 0; i < daysToFetch; i++) {
				LocalDate date = endDate.minusDays(i);
				String dateStr = date.format(dateFormat);
				Map<String, Object> usageOptions = ObjectUtils.asMap("date", dateStr);
				Map<String, Object> historicalUsage = cloudinary.api().usage(usageOptions);

				Map<String, Object> objectsHistorical = (Map<String, Object>) historicalUsage.get("objects");
				Map<String, Object> transformationsHistorical = (Map<String, Object>) historicalUsage
						.get("transformations");

				long historicalStorage = objectsHistorical != null && objectsHistorical.get("used") instanceof Number
						? ((Number) objectsHistorical.get("used")).longValue()
						: 0L;
				long historicalTransformations = transformationsHistorical != null
						&& transformationsHistorical.get("used") instanceof Number
								? ((Number) transformationsHistorical.get("used")).longValue()
								: 0L;

				Map<String, Object> trendEntry = new HashMap<>();
				trendEntry.put("date", dateStr);
				trendEntry.put("storage", historicalStorage / (1024.0 * 1024)); // Convert to MB
				trendEntry.put("transformations", historicalTransformations);
				trendData.add(trendEntry);
			}
			metrics.put("trendData", trendData);

			// Almacenar en caché
			metricsCache.put(cacheKey, metrics);
			logger.info("Métricas de Cloudinary almacenadas en caché");

			return ResponseEntity.ok(metrics);

		} catch (Exception e) {
			logger.error("Error al obtener métricas de Cloudinary", e);
			Map<String, Object> errorResponse = new HashMap<>();
			String errorMessage = e.getMessage();
			if (errorMessage != null && errorMessage.contains("Rate Limit Exceeded")) {
				errorResponse.put("error", "Límite de tasa de Cloudinary excedido. Intenta de nuevo más tarde.");
				return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
			}
			errorResponse.put("error", "Error al obtener métricas de Cloudinary: "
					+ (errorMessage != null ? errorMessage : "Desconocido"));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@GetMapping("/images")
	public ResponseEntity<Map<String, Object>> getCloudinaryImages(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int size) {
		logger.info("Obteniendo imágenes de Cloudinary, página: {}, tamaño: {}", page, size);
		try {
			Map<String, Object> options = ObjectUtils.asMap(
					"resource_type", "image",
					"max_results", size,
					"next_cursor", page > 1 ? getNextCursor(page) : null);
			Map<String, Object> response = cloudinary.api().resources(options);
			logger.info("Cloudinary API response: {}", response);

			List<Map<String, Object>> resources = (List<Map<String, Object>>) response.get("resources");
			if (resources == null) {
				logger.error("Resources list is null in Cloudinary response: {}", response);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(Map.of("error", "Error al obtener imágenes: lista de recursos no encontrada"));
			}

			String nextCursor = (String) response.get("next_cursor");
			Number totalCountNumber = (Number) response.get("total_count");
			long totalCount;
			if (totalCountNumber == null) {
				logger.warn("total_count is missing in Cloudinary response, using resources size as fallback: {}",
						resources.size());
				totalCount = resources.size(); // Fallback to the number of resources returned
			} else {
				totalCount = totalCountNumber.longValue();
			}

			// Almacenar next_cursor en caché para la siguiente página
			if (nextCursor != null) {
				cursorCache.put("page_" + page, nextCursor);
				logger.info("Stored next_cursor for page {}: {}", page, nextCursor);
			} else {
				cursorCache.invalidate("page_" + page);
				logger.info("No next_cursor for page {}, invalidated cache", page);
			}

			Map<String, Object> result = new HashMap<>();
			result.put("images", resources);
			result.put("nextCursor", nextCursor);
			result.put("totalCount", totalCount);
			result.put("currentPage", page);
			result.put("pageSize", size);

			return ResponseEntity.ok(result);
		} catch (Exception e) {
			logger.error("Error al obtener imágenes de Cloudinary", e);
			String errorMessage = e.getMessage();
			if (errorMessage != null && errorMessage.contains("Rate Limit Exceeded")) {
				return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
						.body(Map.of("error", "Límite de tasa de Cloudinary excedido. Intenta de nuevo más tarde."));
			}
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error",
							"Error al obtener imágenes: " + (errorMessage != null ? errorMessage : "Desconocido")));
		}
	}

	@DeleteMapping("/{publicId}")
	public ResponseEntity<Map<String, String>> deleteCloudinaryImage(@PathVariable String publicId) {
		logger.info("Eliminando imagen con publicId: {}", publicId);
		try {
			Map<String, Object> options = ObjectUtils.asMap(
					"resource_type", "image",
					"invalidate", true);
			Map<String, Object> apiResponse = cloudinary.api().deleteResources(Arrays.asList(publicId), options);
			logger.info("Respuesta de Cloudinary: {}", apiResponse);

			// Invalidar caché de métricas y cursores para mantener consistencia
			metricsCache.invalidate("cloudinary_metrics");
			cursorCache.invalidateAll();
			logger.info("Cachés invalidados tras eliminación de imagen con publicId: {}", publicId);

			return ResponseEntity.ok(Map.of("message", "Imagen eliminada exitosamente."));
		} catch (Exception e) {
			logger.error("Error al eliminar imagen con publicId {}: {}", publicId, e.getMessage(), e);
			String errorMessage = e.getMessage();
			if (errorMessage != null && errorMessage.contains("Rate Limit Exceeded")) {
				return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
						.body(Map.of("error", "Límite de tasa de Cloudinary excedido. Intenta de nuevo más tarde."));
			}
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Error al eliminar la imagen: " + e.getMessage()));
		}
	}

	private String getNextCursor(int page) {
		if (page <= 1) {
			logger.debug("No next_cursor needed for page 1");
			return null;
		}
		String cacheKey = "page_" + (page - 1);
		String nextCursor = cursorCache.getIfPresent(cacheKey);
		if (nextCursor == null) {
			logger.warn("No next_cursor found in cache for page {}, starting from beginning", page);
		} else {
			logger.debug("Retrieved next_cursor for page {}: {}", page, nextCursor);
		}
		return nextCursor;
	}
}