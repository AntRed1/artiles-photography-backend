package com.artiles_photography_backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author arojas
 *         DTO para solicitudes de actualización de imágenes del carrusel.
 */
@Data
public class CarouselImageRequest {

	@NotBlank(message = "La URL es obligatoria")
	@Size(max = 255, message = "La URL no puede exceder los 255 caracteres")
	private String url;

	@NotBlank(message = "El título es obligatorio")
	@Size(max = 100, message = "El título no puede exceder los 100 caracteres")
	private String title;

	@NotBlank(message = "La descripción es obligatoria")
	@Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
	private String description;
}