package com.artiles_photography_backend.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author arojas
 */
@Data
public class TestimonialRequest {
	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
	private String name;

	@Min(value = 1, message = "La calificación debe ser al menos 1")
	@Max(value = 5, message = "La calificación no puede exceder 5")
	private int rating;

	@NotBlank(message = "El mensaje es obligatorio")
	@Size(max = 2000, message = "El mensaje no puede exceder los 2000 caracteres")
	private String message;

	private boolean enable;
}
