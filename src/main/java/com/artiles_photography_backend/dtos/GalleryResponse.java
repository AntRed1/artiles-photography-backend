package com.artiles_photography_backend.dtos;

import java.time.LocalDateTime;

import lombok.Data;

/**
 *
 * @author arojas
 */
@Data
public class GalleryResponse {
	private Long id;
	private String imageUrl;
	private String description;
	private LocalDateTime uploadedAt;
}
