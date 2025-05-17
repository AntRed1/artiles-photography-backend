package com.artiles_photography_backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author arojas
 */
@Data
public class LegalRequest {
	@NotBlank(message = "El tipo de documento es obligatorio")
	@Size(max = 100, message = "El tipo no puede exceder los 100 caracteres")
	private String type;

	@NotBlank(message = "El contenido es obligatorio")
	@Size(max = 10000, message = "El contenido no puede exceder los 10000 caracteres")
	private String content;
}
