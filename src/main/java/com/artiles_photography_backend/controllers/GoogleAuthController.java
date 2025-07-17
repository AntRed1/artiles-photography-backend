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

package com.artiles_photography_backend.controllers;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.services.GoogleAuthService;

import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author arojas
 */
@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthController.class);
    private final GoogleAuthService googleAuthService;

    public GoogleAuthController(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    /**
     * Inicia el flujo de autenticación con Google generando una URL de
     * autorización.
     * 
     * @param request Mapa con el email del usuario.
     * @return Respuesta con la URL de autenticación o un mensaje de error.
     */
    @PostMapping("/google")
    public ResponseEntity<Map<String, String>> startGoogleAuth(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            logger.error("Email no proporcionado en la solicitud");
            return ResponseEntity.badRequest().body(Map.of("error", "El email es obligatorio"));
        }
        try {
            String authUrl = googleAuthService.startAuth(email);
            logger.info("URL de autenticación generada para el email: {}", email);
            return ResponseEntity.ok(Map.of("url", authUrl));
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación para el email {}: {}", email, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error generando URL de autenticación para el email {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Error interno al generar la URL de autenticación"));
        }
    }

    /**
     * Procesa el callback de Google OAuth, almacena los tokens y notifica al
     * frontend.
     * 
     * @param code     Código de autorización de Google.
     * @param email    Email del usuario (enviado como state).
     * @param response Respuesta HTTP para enviar el mensaje al frontend.
     */
    @GetMapping("/calendar/callback")
    public void handleCallback(@RequestParam("code") String code, @RequestParam("state") String email,
            HttpServletResponse response) throws IOException {
        if (code == null || email == null || code.trim().isEmpty() || email.trim().isEmpty()) {
            logger.error("Parámetros inválidos en el callback: code={}, email={}", code, email);
            sendErrorResponse(response, "Parámetros inválidos en el callback");
            return;
        }
        try {
            googleAuthService.handleCallback(code, email);
            logger.info("Callback procesado exitosamente para el email: {}", email);
            sendSuccessResponse(response, email);
        } catch (IOException e) {
            logger.error("Error al procesar el callback para el email {}: {}", email, e.getMessage(), e);
            sendErrorResponse(response, "Error al procesar la autenticación: " + e.getMessage());
        }
    }

    /**
     * Cierra la sesión de Google eliminando las credenciales asociadas.
     * 
     * @param request Mapa con el email del usuario.
     * @return Respuesta HTTP indicando el resultado.
     */
    @PostMapping("/signout")
    public ResponseEntity<Map<String, String>> signOutGoogle(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            logger.error("Email no proporcionado en la solicitud de signout");
            return ResponseEntity.badRequest().body(Map.of("error", "El email es obligatorio"));
        }
        try {
            googleAuthService.signOut(email);
            logger.info("Sesión de Google cerrada para el email: {}", email);
            return ResponseEntity.ok(Map.of("message", "Sesión cerrada exitosamente"));
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación para el email {}: {}", email, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error al cerrar sesión de Google para el email {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Error interno al cerrar sesión"));
        }
    }

    /**
     * Envía una respuesta de éxito al frontend mediante postMessage y cierra la
     * ventana.
     * 
     * @param response Respuesta HTTP.
     * @param email    Email del usuario autenticado.
     */
    private void sendSuccessResponse(HttpServletResponse response, String email) throws IOException {
        response.setContentType("text/html");
        response.getWriter().write(
                "<script>" +
                        "window.opener.postMessage({type: 'GOOGLE_AUTH_SUCCESS', email: '" + email + "'}, '*');" +
                        "window.close();" +
                        "</script>");
    }

    /**
     * Envía una respuesta de error al frontend mediante postMessage y cierra la
     * ventana.
     * 
     * @param response     Respuesta HTTP.
     * @param errorMessage Mensaje de error.
     */
    private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        response.setContentType("text/html");
        response.getWriter().write(
                "<script>" +
                        "window.opener.postMessage({type: 'GOOGLE_AUTH_ERROR', message: '" + errorMessage + "'}, '*');"
                        +
                        "window.close();" +
                        "</script>");
    }
}