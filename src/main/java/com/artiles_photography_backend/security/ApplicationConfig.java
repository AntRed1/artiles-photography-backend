package com.artiles_photography_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author arojas
 *         Configuración general de la aplicación, incluyendo beans compartidos
 *         como PasswordEncoder.
 */
@Configuration
public class ApplicationConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}