package com.artiles_photography_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author arojas
 */
@Configuration
public class AppConfig {

	@Bean
	public RestTemplate restTemplate() {

		return new RestTemplate();
	}
}