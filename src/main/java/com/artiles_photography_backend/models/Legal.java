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
@Table(name = "legal")
@Data
public class Legal {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String type; // PRIVACY or TERMS

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	@Column
	private LocalDateTime lastUpdated;

	@Column
	private String updatedBy;
}
