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
import com.artiles_photography_backend.models.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

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
			String name = (userDetails instanceof User) ? ((User) userDetails).getName() : "";
			String token = JWT.create()
					.withSubject(userDetails.getUsername())
					.withIssuer(issuer)
					.withIssuedAt(new Date())
					.withExpiresAt(new Date(System.currentTimeMillis() + expiration))
					.withClaim("name", name)
					.withClaim("roles", userDetails.getAuthorities().stream()
							.map(GrantedAuthority::getAuthority)
							.collect(Collectors.toList()))
					.sign(algorithm);
			logger.debug("Token generado para {}: {}", userDetails.getUsername(), token);
			return token;
		} catch (JWTCreationException e) {
			logger.error("Error al generar token para usuario {}: {}", userDetails.getUsername(), e.getMessage(), e);
			throw new JwtGenerationException("Error al generar el token JWT", e);
		}
	}

	public String getEmailFromToken(String token) {
		try {
			logger.debug("Extrayendo email del token");
			DecodedJWT decodedJWT = JWT.require(algorithm)
					.withIssuer(issuer)
					.build()
					.verify(token);
			String email = decodedJWT.getSubject();
			logger.debug("Email extraído: {}, Issuer: {}, ExpiresAt: {}", email, decodedJWT.getIssuer(),
					decodedJWT.getExpiresAt());
			return email;
		} catch (JWTVerificationException e) {
			logger.warn("Error al extraer email del token: {}", e.getMessage(), e);
			throw new JwtValidationException("Token JWT inválido: " + e.getMessage(), e);
		}
	}

	public boolean validateToken(String token, UserDetails userDetails) {
		try {
			String email = getEmailFromToken(token);
			boolean isExpired = isTokenExpired(token);
			boolean isValid = email.equals(userDetails.getUsername()) && !isExpired;
			logger.debug("Validación de token para {}: isValid={}, emailMatch={}, isExpired={}, authorities={}",
					email, isValid, email.equals(userDetails.getUsername()), isExpired, userDetails.getAuthorities());
			if (!isValid) {
				if (!email.equals(userDetails.getUsername())) {
					logger.warn("El email del token ({}) no coincide con el usuario ({})", email, userDetails.getUsername());
				}
				if (isExpired) {
					logger.warn("Token expirado para usuario: {}", email);
				}
			}
			return isValid;
		} catch (JwtValidationException e) {
			logger.warn("Validación de token fallida: {}", e.getMessage(), e);
			return false;
		}
	}

	private boolean isTokenExpired(String token) {
		try {
			DecodedJWT decodedJWT = JWT.require(algorithm)
					.withIssuer(issuer)
					.build()
					.verify(token);
			Date expirationDate = decodedJWT.getExpiresAt();
			boolean expired = expirationDate.before(new Date());
			logger.debug("Verificación de expiración: expiresAt={}, expired={}", expirationDate, expired);
			return expired;
		} catch (JWTVerificationException e) {
			logger.warn("Error al verificar expiración del token: {}", e.getMessage(), e);
			return true;
		}
	}
}