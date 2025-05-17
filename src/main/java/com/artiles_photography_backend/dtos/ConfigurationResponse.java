package com.artiles_photography_backend.dtos;

import lombok.Data;

/**
 * @author arojas
 */
@Data
public class ConfigurationResponse {
	private Long id;
	private String logoUrl;
	private String logoAltText;
	private String heroBackgroundImage;
	private String availabilityMessage;
	private String responseTime;
	private Boolean notificationsEnabled;
}