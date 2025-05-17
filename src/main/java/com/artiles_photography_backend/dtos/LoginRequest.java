package com.artiles_photography_backend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author arojas
 */
@Data
public class LoginRequest {
	@NotBlank(message = "El email es obligatorio")
	@Email(message = "El email debe ser válido")
	@Size(max = 100, message = "El email no puede exceder los 100 caracteres")
	private String email;

	@NotBlank(message = "La contraseña es obligatoria")
	@Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
	private String password;
}