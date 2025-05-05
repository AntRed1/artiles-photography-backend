package com.artiles_photography_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

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
public class ContactInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "phone", nullable = false)
	private String phone;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "address", nullable = false)
	private String address;

	@Column(name = "whatsapp_number", nullable = false)
	private String whatsapp;

	@Column(name = "facebook", nullable = false)
	private String facebook;

	@Column(name = "instagram", nullable = false)
	private String instagram;

	@Column(name = "twitter", nullable = false)
	private String twitter;

	@Column(name = "tiktok", nullable = false)
	private String tiktok;
}
