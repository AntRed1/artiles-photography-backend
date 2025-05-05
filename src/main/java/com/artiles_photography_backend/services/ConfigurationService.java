package com.artiles_photography_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.Configuration;
import com.artiles_photography_backend.repository.ConfigurationRepository;

/**
 *
 * @author arojas
 */
@Service
public class ConfigurationService {
	@Autowired
	private ConfigurationRepository configurationRepository;

	public Configuration getConfiguration() {
		return configurationRepository.findAll().stream().findFirst()
				.orElseThrow(() -> new RuntimeException("Configuration not found"));
	}

	public String getHeroBackgroundImage() {
		return getConfiguration().getHeroBackgroundImage();
	}

	public Configuration saveConfiguration(Configuration config) {
		return configurationRepository.save(config);
	}
}
