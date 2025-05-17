package com.artiles_photography_backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

/**
 * @author arojas
 *         Configuración para inicializar el cliente de Cloudinary.
 */
@Configuration
public class CloudinaryConfig {

	private static final Logger logger = LoggerFactory.getLogger(CloudinaryConfig.class);

	@Value("${cloudinary.cloud-name}")
	private String cloudName;

	@Value("${cloudinary.api-key}")
	private String apiKey;

	@Value("${cloudinary.api-secret}")
	private String apiSecret;

	@Bean
	public Cloudinary cloudinary() {
		logger.info("Inicializando cliente de Cloudinary con cloudName: {}", cloudName);
		return new Cloudinary(ObjectUtils.asMap(
				"cloud_name", cloudName,
				"api_key", apiKey,
				"api_secret", apiSecret,
				"secure", true));
	}
}