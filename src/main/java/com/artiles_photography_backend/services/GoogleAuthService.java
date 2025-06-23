/*
 * The MIT License
 *
 * Copyright 2025 arojas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.artiles_photography_backend.services;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.GoogleCredential;
import com.artiles_photography_backend.repository.GoogleCredentialRepository;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import jakarta.transaction.Transactional;

/**
 * @author arojas
 *         Servicio para gestionar la autenticación con Google y la interacción
 *         con Google Calendar API.
 */
@Service
@Transactional
@SuppressWarnings("deprecation")
public class GoogleAuthService {
	private static final Logger logger = LoggerFactory.getLogger(GoogleAuthService.class);
	private static final String APPLICATION_NAME = "Artiles Photography";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final long TOKEN_REFRESH_THRESHOLD_SECONDS = 60;

	private final GoogleAuthorizationCodeFlow flow;
	private final GoogleCredentialRepository credentialRepository;
	private final HttpTransport httpTransport;
	private final String redirectUri;
	private final String clientId;
	private final String clientSecret;

	/**
	 * Constructor que inicializa el servicio con las credenciales de Google.
	 */
	public GoogleAuthService(
			@Value("${google.oauth.client-id}") String clientId,
			@Value("${google.oauth.client-secret}") String clientSecret,
			@Value("${google.oauth.redirect-uri}") String redirectUri,
			GoogleCredentialRepository credentialRepository) throws IOException {
		try {
			this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			this.redirectUri = validateRedirectUri(redirectUri);
			this.clientId = Objects.requireNonNull(clientId, "El client-id no puede ser null");
			this.clientSecret = Objects.requireNonNull(clientSecret, "El client-secret no puede ser null");
			this.credentialRepository = Objects.requireNonNull(credentialRepository,
					"El repositorio de credenciales no puede ser null");

			GoogleClientSecrets.Details details = new GoogleClientSecrets.Details()
					.setClientId(clientId)
					.setClientSecret(clientSecret);
			GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setWeb(details);

			this.flow = new GoogleAuthorizationCodeFlow.Builder(
					httpTransport, JSON_FACTORY, clientSecrets,
					Collections.singleton("https://www.googleapis.com/auth/calendar"))
					.setAccessType("offline")
					.setApprovalPrompt("force") // Forzar la obtención de refresh token
					.build();
			logger.info("GoogleAuthService inicializado correctamente");
		} catch (GeneralSecurityException e) {
			logger.error("Error al inicializar GoogleAuthService", e);
			throw new IOException("No se pudo inicializar el servicio de autenticación de Google", e);
		}
	}

	/**
	 * Inicia el flujo de autenticación generando una URL de autorización.
	 * 
	 * @param email Email del usuario.
	 * @return URL de autenticación de Google.
	 */
	public String startAuth(String email) {
		validateEmail(email);
		logger.info("Iniciando flujo de autenticación para el email: {}", email);
		return flow.newAuthorizationUrl()
				.setRedirectUri(redirectUri)
				.setState(email)
				.build();
	}

	/**
	 * Procesa el callback de Google, intercambia el código por tokens y los
	 * almacena.
	 * 
	 * @param code  Código de autorización de Google.
	 * @param email Email del usuario.
	 * @throws IOException Si ocurre un error al procesar el callback.
	 */
	public void handleCallback(String code, String email) throws IOException {
		validateEmail(email);
		Objects.requireNonNull(code, "El código de autorización no puede ser null");
		logger.info("Procesando callback para el email: {}", email);

		try {
			TokenResponse tokenResponse = flow.newTokenRequest(code)
					.setRedirectUri(redirectUri)
					.execute();
			if (tokenResponse.getRefreshToken() == null) {
				logger.warn(
						"No se recibió refresh token para el email: {}. Posible reautenticación sin consentimiento.",
						email);
				throw new IOException("No se recibió refresh token. Reautenticación requerida.");
			}
			GoogleCredential credential = new GoogleCredential(
					email,
					tokenResponse.getAccessToken(),
					tokenResponse.getRefreshToken(),
					tokenResponse.getExpiresInSeconds() != null
							? Instant.now().plusSeconds(tokenResponse.getExpiresInSeconds())
							: null);
			credentialRepository.save(credential);
			logger.info("Credenciales guardadas para el email: {}", email);
		} catch (IOException e) {
			logger.error("Error al procesar el callback para el email: {}. Error: {}", email, e.getMessage(), e);
			throw new IOException("Error al obtener credenciales de Google: " + e.getMessage(), e);
		}
	}

	/**
	 * Cierra la sesión de Google eliminando las credenciales del usuario.
	 * 
	 * @param email Email del usuario.
	 */
	@Transactional
	public void signOut(String email) {
		validateEmail(email);
		logger.info("Cerrando sesión de Google para el email: {}", email);
		Optional<GoogleCredential> optionalCredential = credentialRepository.findByEmail(email);
		if (optionalCredential.isPresent()) {
			credentialRepository.deleteByEmail(email);
			logger.info("Credenciales eliminadas para el email: {}", email);
		} else {
			logger.warn("No se encontraron credenciales para eliminar para el email: {}", email);
		}
	}

	/**
	 * Obtiene un cliente de Google Calendar autenticado para el usuario.
	 * 
	 * @param email Email del usuario.
	 * @return Cliente de Google Calendar.
	 * @throws IOException Si no se encuentran credenciales o el token no es válido.
	 */
	public Calendar getCalendarClient(String email) throws IOException {
		validateEmail(email);
		logger.info("Cargando cliente de Google Calendar para el email: {}", email);

		Optional<GoogleCredential> optionalCredential = credentialRepository.findByEmail(email);
		if (!optionalCredential.isPresent()) {
			logger.error("No se encontraron credenciales para el email: {}", email);
			throw new IOException(
					"No se encontraron credenciales para el email: " + email + ". Reautenticación requerida.");
		}

		GoogleCredential dbCredential = optionalCredential.get();
		if (dbCredential.getRefreshToken() == null) {
			logger.error("No se encontró refresh token para el email: {}", email);
			throw new IOException(
					"No se encontró refresh token para el email: " + email + ". Reautenticación requerida.");
		}

		AccessToken accessToken = new AccessToken(
				dbCredential.getAccessToken(),
				dbCredential.getExpiry() != null ? java.util.Date.from(dbCredential.getExpiry()) : null);

		if (dbCredential.getExpiry() != null &&
				dbCredential.getExpiry().isBefore(Instant.now().plusSeconds(TOKEN_REFRESH_THRESHOLD_SECONDS))) {
			logger.info("Actualizando token de acceso para el email: {}", email);
			try {
				TokenResponse tokenResponse = flow.newTokenRequest(dbCredential.getRefreshToken())
						.setGrantType("refresh_token")
						.execute();
				dbCredential.setAccessToken(tokenResponse.getAccessToken());
				dbCredential.setExpiry(tokenResponse.getExpiresInSeconds() != null
						? Instant.now().plusSeconds(tokenResponse.getExpiresInSeconds())
						: null);
				credentialRepository.save(dbCredential);
				logger.info("Token actualizado para el email: {}", email);
				accessToken = new AccessToken(
						dbCredential.getAccessToken(),
						dbCredential.getExpiry() != null ? java.util.Date.from(dbCredential.getExpiry()) : null);
			} catch (IOException e) {
				logger.error("Error al refrescar el token para el email: {}. Error: {}", email, e.getMessage(), e);
				throw new IOException(
						"Error al refrescar el token para el email: " + email + ". Reautenticación requerida.", e);
			}
		}

		GoogleCredentials credentials = GoogleCredentials.create(accessToken);
		return new Calendar.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
				.setApplicationName(APPLICATION_NAME)
				.build();
	}

	/**
	 * Valida que el email sea válido.
	 * 
	 * @param email Email a validar.
	 */
	private void validateEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			logger.error("Email inválido: null o vacío");
			throw new IllegalArgumentException("El email debe ser proporcionado y válido");
		}
		if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
			logger.error("Formato de email inválido: {}", email);
			throw new IllegalArgumentException("Formato de email inválido");
		}
	}

	/**
	 * Valida que el redirect URI sea válido.
	 * 
	 * @param redirectUri URI a validar.
	 * @return URI validado.
	 */
	private String validateRedirectUri(String redirectUri) {
		if (redirectUri == null || redirectUri.trim().isEmpty()) {
			logger.error("Redirect URI inválido: null o vacío");
			throw new IllegalArgumentException("El redirect URI no puede ser null o vacío");
		}
		return redirectUri;
	}
}