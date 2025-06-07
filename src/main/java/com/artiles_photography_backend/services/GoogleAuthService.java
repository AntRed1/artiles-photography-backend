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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.GoogleCredential;
import com.artiles_photography_backend.repository.GoogleCredentialRepository;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;

/**
 *
 * @author arojas
 */
@Service
public class GoogleAuthService {
	private static final Logger logger = LoggerFactory.getLogger(GoogleAuthService.class);
	private static final String APPLICATION_NAME = "Artiles Photography";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

	private final GoogleAuthorizationCodeFlow flow;
	private final GoogleCredentialRepository credentialRepository;
	private final int oauthPort;

	public GoogleAuthService(GoogleAuthorizationCodeFlow flow,
			GoogleCredentialRepository credentialRepository,
			@Value("${google.calendar.oauth.port:8888}") int oauthPort) {
		this.flow = flow;
		this.credentialRepository = credentialRepository;
		this.oauthPort = oauthPort;
	}

	public String startAuthFlow(String email) {
		AuthorizationCodeRequestUrl authUrl = flow.newAuthorizationUrl()
				.setRedirectUri("http://localhost:" + oauthPort + "/callback");
		return authUrl.build();
	}

	public void handleCallback(String code, String email) throws IOException {
		TokenResponse tokenResponse = flow.newTokenRequest(code)
				.setRedirectUri("http://localhost:" + oauthPort + "/callback")
				.execute();
		Credential credential = flow.createAndStoreCredential(tokenResponse, email);
		GoogleCredential googleCredential = new GoogleCredential();
		googleCredential.setEmail(email);
		googleCredential.setAccessToken(credential.getAccessToken());
		googleCredential.setRefreshToken(credential.getRefreshToken());
		googleCredential.setExpiry(credential.getExpirationTimeMilliseconds());
		credentialRepository.save(googleCredential);
	}

	public void signOut(String email) {
		credentialRepository.deleteById(email);
	}

	public Calendar getCalendarClient(String email) throws IOException {
		logger.info("Cargando credenciales para el email: {}", email);
		Credential credential = flow.loadCredential(email);
		if (credential == null) {
			logger.error("No se encontraron credenciales para el email: {}", email);
			throw new IOException("No credentials found for email: " + email);
		}
		logger.debug("Credenciales cargadas exitosamente para el email: {}", email);
		return new Calendar.Builder(new NetHttpTransport(), JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME)
				.build();
	}
}
