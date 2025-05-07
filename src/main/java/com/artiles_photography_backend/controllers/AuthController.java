package com.artiles_photography_backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.dtos.AuthResponse;
import com.artiles_photography_backend.dtos.LoginRequest;
import com.artiles_photography_backend.dtos.RegisterRequest;
import com.artiles_photography_backend.dtos.UpdateUserRolesRequest;
import com.artiles_photography_backend.dtos.UserResponse;
import com.artiles_photography_backend.services.AuthService;

import jakarta.validation.Valid;

/**
 *
 * @author arojas
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(201).body(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PutMapping("/users/{userId}/roles")
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/admin/users")
	public ResponseEntity<UserResponse> updateUserRoles(
			@PathVariable Long userId, @Valid @RequestBody UpdateUserRolesRequest request) {
		return ResponseEntity.ok(authService.updateUserRoles(userId, request));
	}
}
