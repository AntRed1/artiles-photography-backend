package com.artiles_photography_backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.artiles_photography_backend.dtos.ConfigurationRequest;
import com.artiles_photography_backend.dtos.ConfigurationResponse;
import com.artiles_photography_backend.models.Configuration;
import com.artiles_photography_backend.repository.ConfigurationRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

/**
 *
 * @author arojas
 */
@Service
public class ConfigurationService {

	private final ConfigurationRepository configurationRepository;

	public ConfigurationService(ConfigurationRepository configurationRepository) {
		this.configurationRepository = configurationRepository;
	}

	public ConfigurationResponse getConfiguration() {
		Configuration config = configurationRepository.findById(1L)
				.orElse(null);
		return mapToResponse(config);
	}

	public ConfigurationResponse getConfigurationById(Long id) {
		Configuration config = configurationRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Configuración no encontrada con ID: " + id));
		return mapToResponse(config);
	}

	public String getHeroBackgroundImage() {
		Configuration config = configurationRepository.findById(1L)
				.orElse(null);
		return config != null ? config.getHeroBackgroundImage() : null;
	}

	public List<ConfigurationResponse> getAllConfigurations() {
		return configurationRepository.findAll().stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public ConfigurationResponse createConfiguration(ConfigurationRequest request) {
		Configuration config = new Configuration();
		updateEntityFromRequest(config, request);
		config = configurationRepository.save(config);
		return mapToResponse(config);
	}

	@Transactional
	public ConfigurationResponse updateConfiguration(Long id, ConfigurationRequest request) {
		Configuration config = configurationRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Configuración no encontrada con ID: " + id));
		updateEntityFromRequest(config, request);
		config = configurationRepository.save(config);
		return mapToResponse(config);
	}

	@Transactional
	public void deleteConfiguration(Long id) {
		if (!configurationRepository.existsById(id)) {
			throw new EntityNotFoundException("Configuración no encontrada con ID: " + id);
		}
		configurationRepository.deleteById(id);
	}

	private void updateEntityFromRequest(Configuration config, ConfigurationRequest request) {
		config.setLogoUrl(request.getLogoUrl());
		config.setHeroBackgroundImage(request.getHeroBackgroundImage());
	}

	private ConfigurationResponse mapToResponse(Configuration config) {
		if (config == null) {
			return null;
		}
		ConfigurationResponse response = new ConfigurationResponse();
		response.setId(config.getId());
		response.setLogoUrl(config.getLogoUrl());
		response.setHeroBackgroundImage(config.getHeroBackgroundImage());
		return response;
	}
}