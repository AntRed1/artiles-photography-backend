package com.artiles_photography_backend.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.artiles_photography_backend.services.JwtService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @author arojas
 *         Configuración de seguridad para la aplicación, incluyendo JWT, CORS,
 *         y control de acceso a endpoints.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	public SecurityConfig(JwtService jwtService, UserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

		http
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, authException) -> {
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							response.getWriter().write("{\"error\": \"No autorizado. Por favor, inicia sesión.\"}");
						})
						.accessDeniedHandler((request, response, accessDeniedException) -> {
							response.setStatus(HttpServletResponse.SC_FORBIDDEN);
							response.getWriter()
									.write("{\"error\": \"Acceso denegado. No tienes permisos suficientes.\"}");
						}))
				.authorizeHttpRequests(auth -> auth
						// Permitir acceso a los endpoints de Actuator sin autenticación
						.requestMatchers("/actuator/health", "/actuator/metrics", "/actuator/info").permitAll()
						// Mantener reglas de seguridad para otros endpoints
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/contact-info").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/services").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/testimonials").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/testimonials/all").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/testimonials").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/testimonials/{id}").permitAll()
						.requestMatchers(HttpMethod.PUT, "/api/testimonials/{id}").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/api/testimonials/{id}/toggle-enable").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/testimonials/{id}").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/information").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/information/{id}").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/information/by-title/{title}").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/gallery").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/gallery/{id}").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/gallery/admin/gallery/upload").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/gallery/admin/gallery/{id}").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/gallery/admin/gallery/{id}").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/packages").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/packages/{id}").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/packages/active").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/carousel").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/carousel/upload").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/carousel/{id}").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/carousel/{id}").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/config").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/config/{id}").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/config/hero").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/legal/**").permitAll() // Allow all GET requests under /api/legal
						.requestMatchers(HttpMethod.PUT, "/api/contact-info/admin/{id}").hasRole("ADMIN")
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(
				List.of("http://localhost:5173", "http://localhost:3000", "https://artilesphotography.com"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
}