package com.artiles_photography_backend.dtos;

import java.time.LocalDateTime;

import lombok.Data;

/**
 *
 * @author arojas
 */
@Data
public class TestimonialResponse {
	private Long id;
	private String name;
	private int rating;
	private String message;
	private LocalDateTime createdAt;
	private String device;
	private String ipAddress;
	private String location;
	private boolean enable;
}
