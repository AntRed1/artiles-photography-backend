package com.artiles_photography_backend.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@AllArgsConstructor
@NoArgsConstructor
public class PhotographyService {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String title;

	@Column(nullable = false)
	private String icon;

	// CAMPOS AGREGADOS PARA COMPLETAR LA FUNCIONALIDAD
	@Column(nullable = false)
	private String description;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	// CONSTRUCTOR ADICIONAL para compatibilidad con código existente (solo title e
	// icon)
	public PhotographyService(Long id, String title, String icon) {
		this.id = id;
		this.title = title;
		this.icon = icon;
		this.description = "Descripción por defecto";
		this.price = BigDecimal.ZERO;
	}

	// MÉTODOS AGREGADOS PARA ARREGLAR ERRORES DE COMPILACIÓN
	public String getName() {
		return this.title; // Usando title como nombre
	}

	public String getDescription() {
		return this.description;
	}

	public BigDecimal getPrice() {
		return this.price;
	}
}
