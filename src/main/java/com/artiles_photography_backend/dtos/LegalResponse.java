package com.artiles_photography_backend.dtos;

import lombok.Data;

/**
 *
 * @author arojas
 */
@Data
public class LegalResponse {
	private Long id;
	private String type;
	private String content;
}