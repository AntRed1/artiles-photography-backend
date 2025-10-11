package com.artiles_photography_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author arojas
 *         Configuración CORS completamente basada en variables de entorno
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${cors.allowed-origins}")
	private String[] allowedOrigins;

	@Value("${cors.allowed-methods}")
	private String[] allowedMethods;

	@Value("${cors.allowed-headers}")
	private String[] allowedHeaders;

	@Value("${cors.allow-credentials:true}")
	private boolean allowCredentials;

	@Value("${cors.max-age:3600}")
	private long maxAge;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins(allowedOrigins)
				.allowedMethods(allowedMethods)
				.allowedHeaders(allowedHeaders)
				.allowCredentials(allowCredentials)
				.maxAge(maxAge);
	}
}