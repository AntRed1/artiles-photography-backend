package com.artiles_photography_backend.dtos;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author arojas
 *         DTO para solicitudes de actualización de paquetes fotográficos.
 */
@Data
public class PhotographyPackageRequest {

	@NotBlank(message = "La URL de la imagen es obligatoria")
	@Size(max = 255, message = "La URL no puede exceder los 255 caracteres")
	private String imageUrl;

	@NotBlank(message = "El título es obligatorio")
	@Size(max = 100, message = "El título no puede exceder los 100 caracteres")
	private String title;

	@NotBlank(message = "La descripción es obligatoria")
	@Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
	private String description;

	@NotNull(message = "El precio es obligatorio")
	@Positive(message = "El precio debe ser mayor que 0")
	private Double price;

	@NotNull(message = "El estado activo es obligatorio")
	private Boolean isActive;

	@NotNull(message = "La visibilidad del precio es obligatoria")
	private Boolean showPrice;

	@NotEmpty(message = "Las características son obligatorias")
	@Size(min = 1, max = 20, message = "Debe haber entre 1 y 20 características")
	private List<@NotBlank(message = "Cada característica debe tener contenido") @Size(max = 200, message = "Cada característica no puede exceder los 200 caracteres") String> features;
}