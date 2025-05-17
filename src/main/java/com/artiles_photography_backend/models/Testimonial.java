package com.artiles_photography_backend.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author arojas
 *         * Entidad que representa un testimonio de cliente.
 *
 */
@Entity
@Table(name = "testimonials")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Testimonial {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	@NotBlank
	private String name;

	@Min(1)
	@Max(5)
	@Column(nullable = false)
	private int rating;

	@Column(nullable = false)
	@NotBlank
	private String message;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private String device;

	@Column(nullable = false)
	private String ipAddress;

	@Column(nullable = false)
	private String location;

	@Column(nullable = false)
	private boolean enable = true;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}
}