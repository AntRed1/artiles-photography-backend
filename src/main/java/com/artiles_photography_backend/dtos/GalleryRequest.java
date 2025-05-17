package com.artiles_photography_backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author arojas
 *         DTO para solicitudes de actualización de imágenes de la galería.
 */
@Data
public class GalleryRequest {

	@NotBlank(message = "La URL de la imagen es obligatoria")
	@Size(max = 255, message = "La URL no puede exceder los 255 caracteres")
	private String imageUrl;

	@NotBlank(message = "La descripción es obligatoria")
	@Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
	private String description;
}