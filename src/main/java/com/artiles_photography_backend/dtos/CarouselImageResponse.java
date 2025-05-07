package com.artiles_photography_backend.dtos;

import lombok.Data;

/**
 *
 * @author arojas
 */
@Data
public class CarouselImageResponse {
	private Long id;
	private String url;
	private String title;
	private String description;
	private Integer displayOrder;
}
