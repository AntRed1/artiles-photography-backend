package com.artiles_photography_backend.dtos;

import java.util.Set;

import lombok.Data;

/**
 *
 * @author arojas
 */
@Data
public class AuthResponse {
	private String token;
	private String email;
	private Set<String> roles;
}