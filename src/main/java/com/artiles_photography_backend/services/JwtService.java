package com.artiles_photography_backend.services;

import java.util.Date;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.exceptions.JwtGenerationException;
import com.artiles_photography_backend.exceptions.JwtValidationException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import jakarta.annotation.PostConstruct;

/**
 * @author arojas
 *         Servicio para generar y validar tokens JWT.
 */
@Service
public class JwtService {

	private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

	@Value("${security.jwt.secret}")
	private String secret;

	@Value("${security.jwt.expiration}")
	private long expiration;

	@Value("${security.jwt.issuer:artiles-photography}")
	private String issuer;

	private Algorithm algorithm;

	@PostConstruct
	public void init() {
		this.algorithm = Algorithm.HMAC256(secret);
		logger.info("JwtService inicializado con issuer: {}", issuer);
	}

	public String generateToken(UserDetails userDetails) {
		try {
			logger.debug("Generando token para usuario: {}", userDetails.getUsername());
			return JWT.create()
					.withSubject(userDetails.getUsername())
					.withIssuer(issuer)
					.withIssuedAt(new Date())
					.withExpiresAt(new Date(System.currentTimeMillis() + expiration))
					.withClaim("roles", userDetails.getAuthorities().stream()
							.map(GrantedAuthority::getAuthority)
							.collect(Collectors.toList()))
					.sign(algorithm);
		} catch (JWTCreationException e) {
			logger.error("Error al generar token para usuario {}: {}", userDetails.getUsername(), e.getMessage());
			throw new JwtGenerationException("Error al generar el token JWT", e);
		}
	}

	public String getEmailFromToken(String token) {
		try {
			logger.debug("Extrayendo email del token");
			return JWT.require(algorithm)
					.withIssuer(issuer)
					.build()
					.verify(token)
					.getSubject();
		} catch (JWTVerificationException e) {
			logger.warn("Token JWT inválido: {}", e.getMessage());
			throw new JwtValidationException("Token JWT inválido", e);
		}
	}

	public boolean validateToken(String token, UserDetails userDetails) {
		try {
			String email = getEmailFromToken(token);
			boolean isValid = email.equals(userDetails.getUsername()) && !isTokenExpired(token);
			logger.debug("Validación de token para {}: {}", email, isValid ? "válido" : "inválido");
			return isValid;
		} catch (JwtValidationException e) {
			logger.warn("Validación de token fallida: {}", e.getMessage());
			return false;
		}
	}

	private boolean isTokenExpired(String token) {
		try {
			Date expirationDate = JWT.require(algorithm)
					.withIssuer(issuer)
					.build()
					.verify(token)
					.getExpiresAt();
			boolean expired = expirationDate.before(new Date());
			if (expired) {
				logger.debug("Token expirado en: {}", expirationDate);
			}
			return expired;
		} catch (JWTVerificationException e) {
			logger.warn("Error al verificar expiración del token: {}", e.getMessage());
			return true;
		}
	}
}