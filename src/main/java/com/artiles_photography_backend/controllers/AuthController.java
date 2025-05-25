package com.artiles_photography_backend.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import jakarta.validation.Valid;

/**
 * @author arojas
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final JwtBlacklistRepository jwtBlacklistRepository;

    @Autowired
    public AuthController(AuthService authService, JwtBlacklistRepository jwtBlacklistRepository) {
        this.authService = authService;
        this.jwtBlacklistRepository = jwtBlacklistRepository;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(201).body(authService.register(request));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/auth/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            JwtBlacklist blacklist = new JwtBlacklist();
            blacklist.setToken(token);
            blacklist.setExpiryDate(LocalDateTime.now().plusHours(24)); // Match JWT expiry
            jwtBlacklistRepository.save(blacklist);
        }
        Map<String, String> response = new HashMap<>();
        response.put("message", "Sesión cerrada correctamente");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(201).body(authService.createUser(request));
    }

    @PutMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(authService.updateUser(id, request));
    }

    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Usuario eliminado");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/auth/users/{userId}/roles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> updateUserRoles(
            @PathVariable Long userId, @Valid @RequestBody UpdateUserRolesRequest request) {
        return ResponseEntity.ok(authService.updateUserRoles(userId, request));
    }
}