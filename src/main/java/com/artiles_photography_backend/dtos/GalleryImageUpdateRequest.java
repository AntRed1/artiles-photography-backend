package com.artiles_photography_backend.dtos;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author arojas
 *         DTO para solicitudes de actualización de imágenes de la galería.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GalleryImageUpdateRequest {

	@JsonIgnore
	private MultipartFile file;

	private String title;

	// Para selección de imagen desde Cloudinary
	private String publicId;

	@NotNull(message = "El tipo es obligatorio")
	private String type;

	@NotBlank(message = "La descripción es obligatoria")
	@Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
	private String description;
}