package com.artiles_photography_backend.dtos;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author arojas
 *         DTO para solicitudes de subida de imágenes del carrusel.
 */
@Data
public class CarouselImageUploadRequest {

	@NotNull(message = "El archivo de imagen es obligatorio")
	private MultipartFile file;

	@NotBlank(message = "El título es obligatorio")
	@Size(max = 100, message = "El título no puede exceder los 100 caracteres")
	private String title;

	@NotBlank(message = "La descripción es obligatoria")
	@Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
	private String description;

	@NotNull(message = "El orden de visualización es obligatorio")
	private Integer displayOrder;
}