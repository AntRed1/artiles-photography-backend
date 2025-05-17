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
public class ContactMessageRequest {

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
	private String name;

	@NotBlank(message = "El email es obligatorio")
	@Email(message = "El email debe ser válido")
	@Size(max = 255, message = "El email no puede exceder los 255 caracteres")
	private String email;

	@Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
	private String phone;

	@Size(max = 100, message = "El servicio no puede exceder los 100 caracteres")
	private String service;

	@NotBlank(message = "El mensaje es obligatorio")
	@Size(max = 2000, message = "El mensaje no puede exceder los 2000 caracteres")
	private String message;
}
