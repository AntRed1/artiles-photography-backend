package com.artiles_photography_backend.dtos;

import java.util.List;

import lombok.Data;

/**
 * @author arojas
 */
@Data
public class PhotographyPackageResponse {
	private Long id;
	private String imageUrl;
	private String title;
	private String description;
	private Double price;
	private Boolean isActive;
	private Boolean showPrice;
	private List<String> features;
}