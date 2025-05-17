package com.artiles_photography_backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author arojas
 */
@Data
public class PhotographyServiceRequest {
	@NotBlank(message = "El título es obligatorio")
	@Size(max = 100, message = "El título no puede exceder los 100 caracteres")
	private String title;

	@NotBlank(message = "El icono es obligatorio")
	@Size(max = 255, message = "El icono no puede exceder los 255 caracteres")
	private String icon;
}
