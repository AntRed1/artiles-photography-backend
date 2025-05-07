package com.artiles_photography_backend.dtos;

import java.util.List;

import lombok.Data;

/**
 *
 * @author arojas
 */
@Data
public class AboutUsResponse {
	private Long id;
	private String title;
	private String content;
	private List<String> specialties;
	private List<String> specialtyIcons;
}