package com.artiles_photography_backend.services;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artiles_photography_backend.dtos.AuthResponse;
import com.artiles_photography_backend.dtos.LoginRequest;
import com.artiles_photography_backend.dtos.RegisterRequest;
import com.artiles_photography_backend.dtos.UpdateUserRolesRequest;
import com.artiles_photography_backend.dtos.UserResponse;
import com.artiles_photography_backend.exceptions.AuthenticationFailedException;
import com.artiles_photography_backend.exceptions.EmailAlreadyExistsException;
import com.artiles_photography_backend.models.Role;
import com.artiles_photography_backend.models.User;
import com.artiles_photography_backend.repository.RoleRepository;
import com.artiles_photography_backend.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 *
 * @author arojas
 */
@Service
public class AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public AuthService(
			UserRepository userRepository,
			RoleRepository roleRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			AuthenticationManager authenticationManager) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new EmailAlreadyExistsException("El email ya está registrado");
		}

		User user = new User();
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));

		// Asignar rol USER por defecto
		Role userRole = roleRepository.findByName("ROLE_USER")
				.orElseThrow(() -> new EntityNotFoundException("Rol USER no encontrado"));
		Set<Role> roles = new HashSet<>();
		roles.add(userRole);
		user.setRoles(roles);

		userRepository.save(user);

		String token = jwtService.generateToken(user);
		AuthResponse response = new AuthResponse();
		response.setToken(token);
		response.setEmail(user.getEmail());
		response.setRoles(roles.stream().map(Role::getName).collect(Collectors.toSet()));
		return response;
	}

	public AuthResponse login(LoginRequest request) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		} catch (AuthenticationException e) {
			throw new AuthenticationFailedException("Credenciales inválidas");
		}

		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

		String token = jwtService.generateToken(user);
		AuthResponse response = new AuthResponse();
		response.setToken(token);
		response.setEmail(user.getEmail());
		response.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
		return response;
	}

	@Transactional
	public UserResponse updateUserRoles(Long userId, UpdateUserRolesRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + userId));

		Set<Role> newRoles = new HashSet<>();
		for (String roleName : request.getRoles()) {
			Role role = roleRepository.findByName(roleName)
					.orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + roleName));
			newRoles.add(role);
		}

		user.setRoles(newRoles);
		userRepository.save(user);

		UserResponse response = new UserResponse();
		response.setId(user.getId());
		response.setEmail(user.getEmail());
		response.setRoles(newRoles.stream().map(Role::getName).collect(Collectors.toSet()));
		return response;
	}
}
