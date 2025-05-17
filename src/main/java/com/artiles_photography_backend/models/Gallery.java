package com.artiles_photography_backend.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author arojas
 *         * Entidad que representa una imagen en la galería.
 *
 */
@Entity
@Table(name = "gallery")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Gallery {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(nullable = false)
	private String imageUrl;

	@NotNull
	@Column(nullable = false)
	private String description;

	@NotNull
	@Column(nullable = false)
	private LocalDateTime uploadedAt;

	

}
