package com.artiles_photography_backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.User;
import com.artiles_photography_backend.repository.UserRepository;

/**
 * @author arojas
 *         Implementación de UserDetailsService para cargar detalles de usuario
 *         desde la base de datos.
 *         Convierte el modelo User en un UserDetails compatible con Spring
 *         Security.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		logger.debug("Cargando detalles del usuario con email: {}", email);
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> {
					logger.warn("Usuario no encontrado con email: {}", email);
					return new UsernameNotFoundException("Usuario no encontrado con email: " + email);
				});

		// Si User implementa UserDetails, devolverlo directamente
		if (user instanceof UserDetails userDetails) {
			logger.debug("Usuario {} ya implementa UserDetails", email);
			return userDetails;
		}

		// Construir UserDetails manualmente
		logger.debug("Construyendo UserDetails para usuario: {}", email);
		return org.springframework.security.core.userdetails.User
				.withUsername(user.getEmail())
				.password(user.getPassword())
				.authorities(user.getRoles().stream()
						.map(role -> "ROLE_" + role.getName())
						.toArray(String[]::new))
				.build();
	}
}