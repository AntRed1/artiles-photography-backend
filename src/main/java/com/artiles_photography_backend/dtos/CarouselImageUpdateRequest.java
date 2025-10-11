package com.artiles_photography_backend.dtos;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author arojas
 *         DTO para solicitudes de actualización de imágenes del carrusel.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CarouselImageUpdateRequest {

	private MultipartFile file;

	@NotBlank(message = "El título es obligatorio")
	@Size(max = 100, message = "El título no puede exceder los 100 caracteres")
	private String title;

	@NotBlank(message = "La descripción es obligatoria")
	@Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
	private String description;

	@NotNull(message = "El tipo es obligatorio")
	private String type;

	private String imageUrl;

	private String publicId; // Para imágenes de Cloudinary
}