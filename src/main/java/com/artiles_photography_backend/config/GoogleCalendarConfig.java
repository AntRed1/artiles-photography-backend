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
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
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
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

/**
 * 📅 Configuración para la integración con la API de Google Calendar.
 * 
 * Carga las credenciales, realiza el flujo OAuth2 e instancia el cliente de
 * Calendar.
 * 
 * @author arojas
 */
@Configuration
public class GoogleCalendarConfig {
	private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarConfig.class);
	private static final String APPLICATION_NAME = "Artiles Photography";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

	@Value("${google.calendar.oauth.port:8888}")
	private int oauthPort;

	private final GoogleCredentialRepository credentialRepository;

	public GoogleCalendarConfig(GoogleCredentialRepository credentialRepository) {
		this.credentialRepository = credentialRepository;
	}

	@Bean
	public Calendar googleCalendarService() {
		try {
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, null)
					.setApplicationName(APPLICATION_NAME)
					.build();
		} catch (GeneralSecurityException | IOException e) {
			logger.error("❌ Error initializing Google Calendar service: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to initialize Google Calendar service", e);
		}
	}

	@Bean
	public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow() throws IOException, GeneralSecurityException {
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
				new InputStreamReader(GoogleCalendarConfig.class.getResourceAsStream(CREDENTIALS_FILE_PATH)));
		if (clientSecrets == null) {
			throw new IOException("Google client secrets file not found at " + CREDENTIALS_FILE_PATH);
		}
		return new GoogleAuthorizationCodeFlow.Builder(
				GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets,
				Collections.singletonList(CalendarScopes.CALENDAR))
				.setDataStoreFactory(new JpaDataStoreFactory(credentialRepository))
				.setAccessType("offline")
				.build();
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
				GoogleCredential googleCredential = new GoogleCredential();
				googleCredential.setEmail(key);
				googleCredential.setAccessToken(credential.getAccessToken());
				googleCredential.setRefreshToken(credential.getRefreshToken());
				googleCredential.setExpiry(credential.getExpirationTimeMilliseconds());
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
			credentialRepository.deleteById(key);
			return this;
		}
	}
}