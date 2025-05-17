package com.artiles_photography_backend.dtos;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author arojas
 */
@Data
public class ContactInfoRequest {
	@NotBlank(message = "El teléfono es obligatorio")
	@Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
	private String phone;

	@NotBlank(message = "El email es obligatorio")
	@Email(message = "El email debe ser válido")
	@Size(max = 255, message = "El email no puede exceder los 255 caracteres")
	private String email;

	@NotBlank(message = "La dirección es obligatoria")
	@Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
	private String address;

	@NotBlank(message = "El número de WhatsApp es obligatorio")
	@Size(max = 20, message = "El número de WhatsApp no puede exceder los 20 caracteres")
	private String whatsapp;

	@NotBlank(message = "La URL de Facebook es obligatoria")
	@URL(message = "La URL de Facebook debe ser válida")
	@Size(max = 1000, message = "La URL de Facebook no puede exceder los 1000 caracteres")
	private String facebook;

	@NotBlank(message = "La URL de Instagram es obligatoria")
	@URL(message = "La URL de Instagram debe ser válida")
	@Size(max = 1000, message = "La URL de Instagram no puede exceder los 1000 caracteres")
	private String instagram;

	@NotBlank(message = "La URL de Twitter es obligatoria")
	@URL(message = "La URL de Twitter debe ser válida")
	@Size(max = 1000, message = "La URL de Twitter no puede exceder los 1000 caracteres")
	private String twitter;

	@NotBlank(message = "La URL de TikTok es obligatoria")
	@URL(message = "La URL de TikTok debe ser válida")
	@Size(max = 1000, message = "La URL de TikTok no puede exceder los 1000 caracteres")
	private String tiktok;

	@NotBlank(message = "La URL de Google Maps es obligatoria")
	@Size(max = 1000, message = "La URL de Google Maps no puede exceder los 1000 caracteres")
	private String googleMapsUrl;
}
