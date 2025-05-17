package com.artiles_photography_backend.dtos;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 *
 * @author arojas
 */
@Data
public class UpdateUserRolesRequest {
	@NotEmpty(message = "Los roles no pueden estar vacíos")
	private Set<String> roles;
}
