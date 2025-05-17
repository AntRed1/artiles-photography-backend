package com.artiles_photography_backend.models;

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
 *         * Entidad que representa un documento legal (Política de Privacidad o
 *         Términos).
 * 
 */
@Entity
@Table(name = "legal")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Legal {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String type;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;
}
