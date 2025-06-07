package com.artiles_photography_backend.controllers;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.dtos.AuthResponse;
import com.artiles_photography_backend.dtos.CreateUserRequest;
import com.artiles_photography_backend.dtos.LoginRequest;
import com.artiles_photography_backend.dtos.RegisterRequest;
import com.artiles_photography_backend.dtos.UpdateUserRequest;
import com.artiles_photography_backend.dtos.UpdateUserRolesRequest;
import com.artiles_photography_backend.dtos.UserResponse;
import com.artiles_photography_backend.models.JwtBlacklist;
import com.artiles_photography_backend.repository.JwtBlacklistRepository;
import com.artiles_photography_backend.services.AuthService;
import com.artiles_photography_backend.services.GoogleAuthService;
import com.artiles_photography_backend.services.JwtService;

import jakarta.validation.Valid;

/**
 * @author arojas
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtBlacklistRepository jwtBlacklistRepository;
    private final JwtService jwtService;
    private final GoogleAuthService googleAuthService;

    @Autowired
    public AuthController(
            AuthService authService,
            JwtBlacklistRepository jwtBlacklistRepository,
            JwtService jwtService,
            GoogleAuthService googleAuthService) {
        this.authService = authService;
        this.jwtBlacklistRepository = jwtBlacklistRepository;
        this.jwtService = jwtService;
        this.googleAuthService = googleAuthService;
    }

    private String calculateTokenHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error al calcular el hash del token: {}", e.getMessage(), e);
            throw new RuntimeException("Error al calcular el hash del token", e);
        }
    }

    @PostMapping("/auth/register")
    @ResponseBody
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(201).body(authService.register(request));
    }

    @PostMapping("/auth/login")
    @ResponseBody
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/auth/logout")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        Map<String, String> response = new HashMap<>();
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("error", "Encabezado de autorización inválido");
            return ResponseEntity.status(400).body(response);
        }
        String token = authHeader.substring(7);
        String tokenHash = calculateTokenHash(token);
        if (jwtBlacklistRepository.existsByTokenHash(tokenHash)) {
            response.put("message", "El token ya ha sido invalidado");
            return ResponseEntity.ok(response);
        }
        try {
            String email = jwtService.getEmailFromToken(token);
            if (email == null) {
                response.put("error", "Token inválido: no se pudo extraer el email");
                return ResponseEntity.status(400).body(response);
            }
            JwtBlacklist blacklist = new JwtBlacklist();
            blacklist.setToken(token);
            blacklist.setTokenHash(tokenHash);
            LocalDateTime expiryDate = jwtService.getExpirationFromToken(token);
            if (expiryDate == null) {
                expiryDate = LocalDateTime.now().plusHours(24);
            }
            blacklist.setExpiryDate(expiryDate);
            jwtBlacklistRepository.save(blacklist);
            response.put("message", "Sesión cerrada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al cerrar sesión: {}", e.getMessage(), e);
            response.put("error", "Error al cerrar sesión: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseBody
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseBody
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(201).body(authService.createUser(request));
    }

    @PutMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseBody
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(authService.updateUser(id, request));
    }

    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Usuario eliminado");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/auth/users/{userId}/roles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseBody
    public ResponseEntity<UserResponse> updateUserRoles(
            @PathVariable Long userId, @Valid @RequestBody UpdateUserRolesRequest request) {
        return ResponseEntity.ok(authService.updateUserRoles(userId, request));
    }

    @GetMapping("/callback")
    public String handleGoogleCallback(@RequestParam String code, @RequestParam String email) {
        try {
            googleAuthService.handleCallback(code, email);
            return "auth-callback";
        } catch (IOException e) {
            logger.error("Error procesando callback para el email: {}", email, e);
            return "auth-callback-error";
        }
    }
}