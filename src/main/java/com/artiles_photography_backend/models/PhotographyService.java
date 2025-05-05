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
 *         * Entidad que representa un servicio fotográfico adicional ofrecido
 *         por Artiles Photography.
 *
 */
@Entity
@Table(name = "photography_services")
@Data
public class PhotographyService {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String icon;

	// Constructor por defecto requerido por JPA
	public PhotographyService() {
	}

	// Constructor para inicialización en DataInitializer
	public PhotographyService(String title, String icon) {
		this.title = title;
		this.icon = icon;
	}
}
