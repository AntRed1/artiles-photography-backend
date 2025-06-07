package com.artiles_photography_backend.security;

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

import com.artiles_photography_backend.repository.JwtBlacklistRepository;
import com.artiles_photography_backend.services.JwtService;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

/**
 * @author arojas
 *         Configuración de seguridad para la aplicación, incluyendo JWT, CORS,
 *         y control de acceso a endpoints, con soporte para Google Calendar
 *         API.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;
	private final JwtBlacklistRepository jwtBlacklistRepository;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(
			JwtService jwtService,
			UserDetailsService userDetailsService,
			JwtBlacklistRepository jwtBlacklistRepository,
			JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
		this.jwtBlacklistRepository = jwtBlacklistRepository;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, authException) -> {
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							response.setContentType("application/json");
							response.setCharacterEncoding("UTF-8");
							response.getWriter().write("{\"error\": \"No autorizado. Por favor, inicia sesión.\"}");
						})
						.accessDeniedHandler((request, response, accessDeniedException) -> {
							response.setStatus(HttpServletResponse.SC_FORBIDDEN);
							response.setContentType("application/json");
							response.setCharacterEncoding("UTF-8");
							response.getWriter()
									.write("{\"error\": \"Acceso denegado. No tienes permisos suficientes.\"}");
						}))
				.authorizeHttpRequests(auth -> auth
						// Public endpoints
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/actuator/health").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/auth/google").permitAll()
						.requestMatchers(HttpMethod.GET, "/callback").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/contact-info").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/services").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/testimonials", "/api/testimonials/all").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/testimonials").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/testimonials/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/information", "/api/information/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/gallery", "/api/gallery/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/packages", "/api/packages/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/carousel").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/config", "/api/config/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/legal/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/cloudinary-metrics").permitAll()
						.requestMatchers(HttpMethod.DELETE, "/api/cloudinary-metrics/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/cloudinary-metrics/images").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/google").permitAll()
						// Authenticated endpoints
						.requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/auth/signout").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/calendar/events").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/analytics/notifications").authenticated()
						// Admin-only endpoints
						.requestMatchers("/actuator/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/contact-info/admin").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/testimonials/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/api/testimonials/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/testimonials/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/gallery/admin/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/gallery/admin/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/gallery/admin/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/carousel/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/carousel/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/carousel/**").hasRole("ADMIN")
						.requestMatchers("/api/analytics/**").hasRole("ADMIN")
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						// All other requests require authentication
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "https://artilesphotography.com",
				"http://localhost:3000", "http://localhost:5173", "http://localhost/", "http://artiles.local:8080",
				"http://artiles.local:3000", "http://3.144.121.87"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}