package com.artiles_photography_backend.dtos;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author arojas
 *         DTO para solicitudes de subida de imágenes de la galería.
 */
@Data
public class GalleryUploadRequest {

	@NotNull(message = "El archivo de imagen es obligatorio")
	private MultipartFile file;

	@NotBlank(message = "La descripción es obligatoria")
	@Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
	private String description;
}