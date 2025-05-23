package com.artiles_photography_backend.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.artiles_photography_backend.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author arojas
 *         Filtro para autenticar solicitudes usando tokens JWT.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authHeader = request.getHeader("Authorization");
		logger.debug("Procesando solicitud: {} {}, Authorization: {}",
				request.getMethod(), request.getRequestURI(), authHeader != null ? authHeader : "No presente");

		// Skip if already authenticated
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			logger.debug("Autenticación ya presente para {} {}, continuando con la cadena de filtros",
					request.getMethod(), request.getRequestURI());
			filterChain.doFilter(request, response);
			return;
		}

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			logger.debug("No se encontró token JWT en la solicitud: {} {}",
					request.getMethod(), request.getRequestURI());
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(7);
		logger.debug("Token JWT extraído: {}", token);

		try {
			String email = jwtService.getEmailFromToken(token);
			logger.debug("Email extraído del token: {}", email);

			if (email != null) {
				logger.debug("Cargando UserDetails para email: {}", email);
				UserDetails userDetails = userDetailsService.loadUserByUsername(email);
				logger.debug("UserDetails cargado: {}, Authorities: {}",
						userDetails.getUsername(), userDetails.getAuthorities());

				logger.debug("Validando token para usuario: {}", email);
				if (jwtService.validateToken(token, userDetails)) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
					logger.info("Autenticación exitosa para usuario: {}, Authorities: {}",
							email, userDetails.getAuthorities());
				} else {
					logger.warn("Token JWT inválido para usuario: {}", email);
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.setContentType("application/json");
					response.getWriter().write("{\"error\": \"Token JWT inválido\"}");
					return;
				}
			} else {
				logger.warn("No se pudo extraer email del token para {} {}",
						request.getMethod(), request.getRequestURI());
				filterChain.doFilter(request, response);
				return;
			}
		} catch (UsernameNotFoundException e) {
			logger.error("Usuario no encontrado para el token: {}", e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			response.getWriter().write("{\"error\": \"Usuario no encontrado\"}");
			return;
		} catch (ServletException | IOException e) {
			logger.error("Error al procesar token JWT: {}", e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			response.getWriter().write("{\"error\": \"Error al procesar el token JWT: " + e.getMessage() + "\"}");
			return;
		}

		logger.debug("Continuando con la cadena de filtros para {} {}",
				request.getMethod(), request.getRequestURI());
		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		String method = request.getMethod();
		boolean shouldNotFilter = path.startsWith("/api/auth") ||
				(path.equals("/api/contact") && method.equals("POST")) ||
				path.startsWith("/api/services") ||
				(path.startsWith("/api/testimonials") && (method.equals("GET") || method.equals("POST"))) ||
				path.startsWith("/api/information") ||
				(path.startsWith("/api/gallery") && method.equals("GET")) ||
				(path.equals("/api/packages") && method.equals("GET")) ||
				(path.startsWith("/api/carousel") && method.equals("GET")) ||
				(path.equals("/api/config") && method.equals("GET")) ||
				(path.startsWith("/api/legal") && method.equals("GET")) ||
				(path.equals("/api/contact-info") && method.equals("GET"));
		logger.debug("shouldNotFilter para {} {}: {}", method, path, shouldNotFilter);
		return shouldNotFilter;
	}
}