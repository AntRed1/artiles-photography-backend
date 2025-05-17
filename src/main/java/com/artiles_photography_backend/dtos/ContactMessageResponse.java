package com.artiles_photography_backend.dtos;

import java.time.LocalDateTime;

import lombok.Data;

/**
 *
 * @author arojas
 */
@Data
public class ContactMessageResponse {
	private Long id;
	private String name;
	private String email;
	private String phone;
	private String service;
	private String message;
	private String clientIp;
	private String userAgent;
	private LocalDateTime createdAt;
}