package com.artiles_photography_backend.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.artiles_photography_backend.dtos.ConfigurationRequest;
import com.artiles_photography_backend.dtos.ConfigurationResponse;
import com.artiles_photography_backend.dtos.ConfigurationUploadRequest;
import com.artiles_photography_backend.exceptions.CloudinaryUploadException;
import com.artiles_photography_backend.models.Configuration;
import com.artiles_photography_backend.repository.ConfigurationRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

/**
 * @author arojas
 */
@Service
public class ConfigurationService {

	private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
	private static final String CLOUDINARY_FOLDER = "photoquince/logo";
	private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
	private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/svg+xml");

	private final ConfigurationRepository configurationRepository;
	private final Cloudinary cloudinary;

	public ConfigurationService(ConfigurationRepository configurationRepository, Cloudinary cloudinary) {
		this.configurationRepository = configurationRepository;
		this.cloudinary = cloudinary;
	}

	public ConfigurationResponse getConfiguration() {
		Configuration config = configurationRepository.findById(1L).orElse(null);
		return mapToResponse(config);
	}

	public ConfigurationResponse getConfigurationById(Long id) {
		Configuration config = configurationRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Configuración no encontrada con ID: " + id));
		return mapToResponse(config);
	}

	public String getHeroBackgroundImage() {
		Configuration config = configurationRepository.findById(1L).orElse(null);
		return config != null ? config.getHeroBackgroundImage() : null;
	}

	public List<ConfigurationResponse> getAllConfigurations() {
		return configurationRepository.findAll().stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public ConfigurationResponse createConfiguration(ConfigurationUploadRequest request) {
		logger.info("Creando nueva configuración");
		validateFile(request.getLogoFile(), true);

		try {
			Map uploadResult = cloudinary.uploader().upload(request.getLogoFile().getBytes(),
					ObjectUtils.asMap("folder", CLOUDINARY_FOLDER));
			String url = (String) uploadResult.get("secure_url");
			String publicId = (String) uploadResult.get("public_id");

			Configuration config = new Configuration();
			config.setLogoUrl(url);
			config.setLogoPublicId(publicId);
			updateEntityFromUploadRequest(config, request);
			config = configurationRepository.save(config);

			logger.info("Configuración creada y logo subido a Cloudinary con public_id: {}", publicId);
			return mapToResponse(config);
		} catch (IOException e) {
			logger.error("Error al subir logo a Cloudinary: {}", e.getMessage());
			throw new CloudinaryUploadException("Error al subir el logo a Cloudinary", e);
		}
	}

	@Transactional
	public ConfigurationResponse updateConfiguration(Long id, ConfigurationUploadRequest request) {
		logger.info("Actualizando configuración con ID: {}", id);
		Configuration config = configurationRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Configuración no encontrada con ID: " + id));

		// Handle logo file if provided
		MultipartFile file = request.getLogoFile();
		if (file != null && !file.isEmpty()) {
			validateFile(file, false);
			try {
				// Delete old logo from Cloudinary if it exists
				if (config.getLogoPublicId() != null) {
					cloudinary.uploader().destroy(config.getLogoPublicId(), ObjectUtils.emptyMap());
					logger.info("Logo anterior eliminado de Cloudinary con public_id: {}", config.getLogoPublicId());
				}

				// Upload new logo
				Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
						ObjectUtils.asMap("folder", CLOUDINARY_FOLDER));
				String url = (String) uploadResult.get("secure_url");
				String publicId = (String) uploadResult.get("public_id");
				config.setLogoUrl(url);
				config.setLogoPublicId(publicId);
				logger.info("Nuevo logo subido a Cloudinary con public_id: {}", publicId);
			} catch (IOException e) {
				logger.error("Error al subir logo a Cloudinary: {}", e.getMessage());
				throw new CloudinaryUploadException("Error al subir el logo a Cloudinary", e);
			}
		}

		updateEntityFromUploadRequest(config, request);
		config = configurationRepository.save(config);
		return mapToResponse(config);
	}

	@Transactional
	public ConfigurationResponse updateConfiguration(Long id, ConfigurationRequest request) {
		logger.info("Actualizando configuración con ID: {} (sin archivo)", id);
		Configuration config = configurationRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Configuración no encontrada con ID: " + id));
		updateEntityFromRequest(config, request);
		config = configurationRepository.save(config);
		return mapToResponse(config);
	}

	@Transactional
	public void deleteConfiguration(Long id) {
		logger.info("Eliminando configuración con ID: {}", id);
		Configuration config = configurationRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Configuración no encontrada con ID: " + id));

		try {
			if (config.getLogoPublicId() != null) {
				cloudinary.uploader().destroy(config.getLogoPublicId(), ObjectUtils.emptyMap());
				logger.info("Logo eliminado de Cloudinary con public_id: {}", config.getLogoPublicId());
			}
		} catch (IOException e) {
			logger.error("Error al eliminar logo de Cloudinary: {}", e.getMessage());
			throw new CloudinaryUploadException("Error al eliminar el logo de Cloudinary", e);
		}

		configurationRepository.deleteById(id);
	}

	private void validateFile(MultipartFile file, boolean required) {
		if (required && (file == null || file.isEmpty())) {
			logger.warn("Archivo de logo vacío o nulo");
			throw new IllegalArgumentException("El archivo de logo es obligatorio");
		}
		if (file != null && !file.isEmpty()) {
			if (file.getSize() > MAX_FILE_SIZE) {
				logger.warn("Archivo de logo excede el tamaño máximo: {} bytes", file.getSize());
				throw new IllegalArgumentException("El archivo excede el tamaño máximo de 2MB");
			}
			if (!ALLOWED_TYPES.contains(file.getContentType())) {
				logger.warn("Tipo de archivo no permitido: {}", file.getContentType());
				throw new IllegalArgumentException("Solo se permiten imágenes JPEG, PNG o SVG");
			}
		}
	}

	private void updateEntityFromRequest(Configuration config, ConfigurationRequest request) {
		config.setLogoUrl(request.getLogoUrl());
		config.setLogoAltText(request.getLogoAltText());
		config.setHeroBackgroundImage(request.getHeroBackgroundImage());
		config.setAvailabilityMessage(request.getAvailabilityMessage());
		config.setResponseTime(request.getResponseTime());
		config.setNotificationsEnabled(request.getNotificationsEnabled());
	}

	private void updateEntityFromUploadRequest(Configuration config, ConfigurationUploadRequest request) {
		config.setLogoAltText(request.getLogoAltText());
		config.setHeroBackgroundImage(request.getHeroBackgroundImage());
		config.setAvailabilityMessage(request.getAvailabilityMessage());
		config.setResponseTime(request.getResponseTime());
		config.setNotificationsEnabled(request.getNotificationsEnabled());
	}

	private ConfigurationResponse mapToResponse(Configuration config) {
		if (config == null) {
			return null;
		}
		ConfigurationResponse response = new ConfigurationResponse();
		response.setId(config.getId());
		response.setLogoUrl(config.getLogoUrl());
		response.setLogoAltText(config.getLogoAltText());
		response.setHeroBackgroundImage(config.getHeroBackgroundImage());
		response.setAvailabilityMessage(config.getAvailabilityMessage());
		response.setResponseTime(config.getResponseTime());
		response.setNotificationsEnabled(config.getNotificationsEnabled());
		return response;
	}
}