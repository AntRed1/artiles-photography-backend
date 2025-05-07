package com.artiles_photography_backend.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artiles_photography_backend.dtos.AuthResponse;
import com.artiles_photography_backend.dtos.CreateUserRequest;
import com.artiles_photography_backend.dtos.LoginRequest;
import com.artiles_photography_backend.dtos.RegisterRequest;
import com.artiles_photography_backend.dtos.UpdateUserRequest;
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
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setEnabled(true);

		Role userRole = roleRepository.findByName("USER")
				.orElseThrow(() -> new EntityNotFoundException("Rol USER no encontrado"));
		Set<Role> roles = new HashSet<>();
		roles.add(userRole);
		user.setRoles(roles);

		userRepository.save(user);

		String token = jwtService.generateToken(user);
		AuthResponse response = new AuthResponse();
		response.setToken(token);
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		response.setRoles(roles.stream().map(Role::getName).collect(Collectors.toSet()));
		return response;
	}

	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new AuthenticationFailedException("Credenciales inválidas"));

		if (!user.isEnabled()) {
			throw new AuthenticationFailedException("La cuenta está desactivada");
		}

		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		} catch (DisabledException e) {
			throw new AuthenticationFailedException("La cuenta está desactivada");
		} catch (AuthenticationException e) {
			throw new AuthenticationFailedException("Credenciales inválidas");
		}

		String token = jwtService.generateToken(user);
		AuthResponse response = new AuthResponse();
		response.setToken(token);
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		response.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
		return response;
	}

	@Transactional
	public List<UserResponse> getAllUsers() {
		return userRepository.findAll().stream()
				.map(user -> new UserResponse(
						user.getId(),
						user.getName(),
						user.getEmail(),
						user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
						user.isEnabled()))
				.collect(Collectors.toList());
	}

	@Transactional
	public UserResponse createUser(CreateUserRequest request) {
		if (userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new EmailAlreadyExistsException("El email ya está registrado");
		}

		User user = new User();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setEnabled(request.isEnabled());

		String roleName = mapRoleToBackend(request.getRole());
		Role role = roleRepository.findByName(roleName)
				.orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + roleName));
		Set<Role> roles = new HashSet<>();
		roles.add(role);
		user.setRoles(roles);

		userRepository.save(user);

		return new UserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				roles.stream().map(Role::getName).collect(Collectors.toSet()),
				user.isEnabled());
	}

	@Transactional
	public UserResponse updateUser(Long id, UpdateUserRequest request) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

		if (request.getName() != null && !request.getName().isBlank()) {
			user.setName(request.getName());
		}
		if (request.getEmail() != null && !request.getEmail().isBlank()) {
			if (!request.getEmail().equals(user.getEmail())
					&& userRepository.findByEmail(request.getEmail()).isPresent()) {
				throw new EmailAlreadyExistsException("El email ya está registrado");
			}
			user.setEmail(request.getEmail());
		}
		if (request.getPassword() != null && !request.getPassword().isBlank()) {
			user.setPassword(passwordEncoder.encode(request.getPassword()));
		}
		if (request.getRole() != null && !request.getRole().isBlank()) {
			String roleName = mapRoleToBackend(request.getRole());
			Role role = roleRepository.findByName(roleName)
					.orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + roleName));
			Set<Role> roles = new HashSet<>();
			roles.add(role);
			user.setRoles(roles);
		}
		if (request.getEnabled() != null) {
			user.setEnabled(request.getEnabled());
		}

		userRepository.save(user);

		return new UserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
				user.isEnabled());
	}

	@Transactional
	public void deleteUser(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
		userRepository.delete(user);
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

		return new UserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				newRoles.stream().map(Role::getName).collect(Collectors.toSet()),
				user.isEnabled());
	}

	private String mapRoleToBackend(String frontendRole) {
		return switch (frontendRole) {
			case "Administrador" -> "ADMIN";
			case "Editor" -> "EDITOR";
			case "Visualizador" -> "VISUALIZADOR";
			default -> throw new IllegalArgumentException("Rol no válido: " + frontendRole);
		};
	}
}