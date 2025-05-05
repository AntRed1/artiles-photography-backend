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
 *         Entidad que representa un paquete fotográfico ofrecido por Artiles
 *         Photography.
 *
 */
@Entity
@Table(name = "photography_packages")
@Data
public class PhotographyPackage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String description;

	@Column(nullable = false)
	private Double price;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive;

	@Column(nullable = false)
	private String imageUrl;

	// Constructor por defecto requerido por JPA
	public PhotographyPackage() {
	}

	// Constructor para inicialización en DataInitializer
	public PhotographyPackage(String title, String description, Double price, Boolean isActive, String imageUrl) {
		this.title = title;
		this.description = description;
		this.price = price;
		this.isActive = isActive;
		this.imageUrl = imageUrl;
	}
}