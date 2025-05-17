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
 * @author arojas
 */
@Entity
@Table(name = "configuration")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Configuration {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String logoUrl;

	@Column
	private String logoPublicId;

	@Column
	private String logoAltText;

	@Column
	private String heroBackgroundImage;

	@Column
	private String availabilityMessage;

	@Column
	private String responseTime;

	@Column
	private Boolean notificationsEnabled;
}