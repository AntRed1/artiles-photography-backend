package com.artiles_photography_backend.dtos;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author arojas
 */
@Data
public class AboutUsRequest {
	@NotBlank(message = "El título es obligatorio")
	@Size(max = 100, message = "El título no puede exceder los 100 caracteres")
	private String title;

	@NotBlank(message = "El contenido es obligatorio")
	@Size(max = 10000, message = "El contenido no puede exceder los 10000 caracteres")
	private String content;

	@NotEmpty(message = "Las especialidades no pueden estar vacías")
	@Size(max = 50, message = "No se pueden incluir más de 50 especialidades")
	private List<@Size(max = 255, message = "Cada especialidad no puede exceder los 255 caracteres") String> specialties;

	@NotEmpty(message = "Los iconos de especialidades no pueden estar vacíos")
	@Size(max = 50, message = "No se pueden incluir más de 50 iconos de especialidades")
	private List<@Size(max = 255, message = "Cada icono no puede exceder los 255 caracteres") String> specialtyIcons;
}
