package com.artiles_photography_backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author arojas
 */
@Data
public class ConfigurationRequest {
    @NotBlank(message = "La URL del logo es obligatoria")
    @Size(max = 255, message = "La URL del logo no puede exceder los 255 caracteres")
    private String logoUrl;

    @Size(max = 255, message = "El texto alternativo del logo no puede exceder los 255 caracteres")
    private String logoAltText;

    @Size(max = 255, message = "La URL de la imagen de fondo del hero no puede exceder los 255 caracteres")
    private String heroBackgroundImage;

    @Size(max = 500, message = "El mensaje de disponibilidad no puede exceder los 500 caracteres")
    private String availabilityMessage;

    @Size(max = 50, message = "El tiempo de respuesta no puede exceder los 50 caracteres")
    private String responseTime;

    private Boolean notificationsEnabled;
}