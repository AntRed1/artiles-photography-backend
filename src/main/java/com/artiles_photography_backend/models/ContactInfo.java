package com.artiles_photography_backend.models;

import org.hibernate.validator.constraints.URL;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author arojas
 *         * Entidad que representa la información de contacto de Artiles
 *         Photography.
 *
 */
@Entity
@Table(name = "contact_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "phone", nullable = false)
	private String phone;

	@Column(name = "email", nullable = false)
	@Email
	private String email;

	@Column(name = "address", nullable = false)
	private String address;

	@Column(name = "whatsapp_number", nullable = false)
	private String whatsapp;

	@Column(name = "facebook", nullable = false, columnDefinition = "TEXT")
	@URL
	private String facebook;

	@Column(name = "instagram", nullable = false)
	@URL
	private String instagram;

	@Column(name = "twitter", nullable = false)
	@URL
	private String twitter;

	@Column(name = "tiktok", nullable = false)
	@URL
	private String tiktok;

	@Column(name = "googleMapsUrl", nullable = false, columnDefinition = "TEXT")
	private String googleMapsUrl;
}
