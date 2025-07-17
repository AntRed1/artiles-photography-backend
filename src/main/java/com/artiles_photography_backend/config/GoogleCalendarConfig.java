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

package com.artiles_photography_backend.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.artiles_photography_backend.models.GoogleCredential;
import com.artiles_photography_backend.repository.GoogleCredentialRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;

/**
 * Configuración para la integración con la API de Google Calendar.
 * Carga las credenciales, realiza el flujo OAuth2 e instancia el cliente de
 * Calendar.
 */
@Configuration
@SuppressWarnings("deprecation")
public class GoogleCalendarConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCalendarConfig.class);
	private static final String APPLICATION_NAME = "Artiles Photography";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	private final GoogleCredentialRepository credentialRepository;

	@Value("${google.oauth.client-id}")
	private String clientId;

	@Value("${google.oauth.client-secret}")
	private String clientSecret;

	@Value("${google.oauth.redirect-uri}")
	private String redirectUri;

	public GoogleCalendarConfig(GoogleCredentialRepository credentialRepository) {
		this.credentialRepository = credentialRepository;
	}

	@Bean
	public HttpTransport httpTransport() throws GeneralSecurityException, IOException {
		return GoogleNetHttpTransport.newTrustedTransport();
	}

	@Bean
	public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow(HttpTransport httpTransport) {
		try {
			GoogleClientSecrets.Details details = new GoogleClientSecrets.Details()
					.setClientId(clientId)
					.setClientSecret(clientSecret)
					.setRedirectUris(Collections.singletonList(redirectUri));
			GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setWeb(details);

			return new GoogleAuthorizationCodeFlow.Builder(
					httpTransport,
					JSON_FACTORY,
					clientSecrets,
					Collections.singletonList(CalendarScopes.CALENDAR))
					.setDataStoreFactory(new JpaDataStoreFactory(credentialRepository))
					.setAccessType("offline")
					.build();
		} catch (IOException e) {
			LOGGER.error("Error al configurar el flujo de autorización: {}", e.getMessage(), e);
			throw new RuntimeException("Error al configurar el flujo de autorización", e);
		}
	}

	static class JpaDataStoreFactory extends AbstractDataStoreFactory {
		private final GoogleCredentialRepository credentialRepository;

		JpaDataStoreFactory(GoogleCredentialRepository credentialRepository) {
			this.credentialRepository = credentialRepository;
		}

		@Override
		protected <V extends java.io.Serializable> DataStore<V> createDataStore(String id) {
			return new JpaDataStore<>(this, id, credentialRepository);
		}
	}

	static class JpaDataStore<V extends java.io.Serializable> implements DataStore<V> {
		private final JpaDataStoreFactory factory;
		private final String id;
		private final GoogleCredentialRepository credentialRepository;

		JpaDataStore(JpaDataStoreFactory factory, String id, GoogleCredentialRepository credentialRepository) {
			this.factory = factory;
			this.id = id;
			this.credentialRepository = credentialRepository;
		}

		@Override
		public DataStoreFactory getDataStoreFactory() {
			return factory;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public int size() {
			return (int) credentialRepository.count();
		}

		@Override
		public boolean isEmpty() {
			return credentialRepository.count() == 0;
		}

		@Override
		public boolean containsKey(String key) {
			return credentialRepository.existsById(key);
		}

		@Override
		public boolean containsValue(V value) {
			return false; // No implementado
		}

		@Override
		public java.util.Set<String> keySet() {
			return credentialRepository.findAll().stream()
					.map(GoogleCredential::getEmail)
					.collect(java.util.stream.Collectors.toSet());
		}

		@Override
		public java.util.Collection<V> values() {
			return null; // No implementado
		}

		@Override
		public V get(String key) {
			return null; // No implementado
		}

		@Override
		public DataStore<V> set(String key, V value) {
			if (value instanceof Credential credential) {
				GoogleCredential googleCredential = credentialRepository.findByEmail(key)
						.orElse(new GoogleCredential());
				googleCredential.setEmail(key);
				googleCredential.setAccessToken(credential.getAccessToken());
				googleCredential.setRefreshToken(credential.getRefreshToken());
				Long expiryMillis = credential.getExpirationTimeMilliseconds();
				googleCredential.setExpiry(expiryMillis != null ? Instant.ofEpochMilli(expiryMillis) : null);
				credentialRepository.save(googleCredential);
			}
			return this;
		}

		@Override
		public DataStore<V> clear() {
			credentialRepository.deleteAll();
			return this;
		}

		@Override
		public DataStore<V> delete(String key) {
			credentialRepository.deleteByEmail(key);
			return this;
		}
	}
}