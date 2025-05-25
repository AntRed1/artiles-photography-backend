package com.artiles_photography_backend.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

/**
 * @author arojas
 *         Controlador para obtener métricas de uso de Cloudinary.
 *         Retorna información como total de imágenes, uso de almacenamiento,
 *         subidas recientes y tendencias históricas.
 */
@RestController
@RequestMapping("/api/cloudinary-metrics")
public class CloudinaryMetricsController {

	@Autowired
	private Cloudinary cloudinary;

	@GetMapping
	public Map<String, Object> getCloudinaryMetrics() {
		Map<String, Object> metrics = new HashMap<>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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

			// Historical trend data for the last 7 days
			List<Map<String, Object>> trendData = new ArrayList<>();
			Date endDate = new Date(); // Current date: 2025-05-24
			for (int i = 6; i >= 0; i--) {
				Date date = new Date(endDate.getTime() - i * 24 * 60 * 60 * 1000); // Subtract i days
				String dateStr = dateFormat.format(date);
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

			return metrics;

		} catch (Exception e) {
			throw new RuntimeException("Error al obtener métricas de Cloudinary: " + e.getMessage(), e);
		}
	}
}