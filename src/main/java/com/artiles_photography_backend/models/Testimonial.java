package com.artiles_photography_backend.models;

import java.time.LocalDateTime;

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
 */
@Entity
@Table(name = "testimonials")
@Data
public class Testimonial {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String message;

	@Column(nullable = false)
	private Integer rating;

	@Column
	private LocalDateTime createdAt;
}