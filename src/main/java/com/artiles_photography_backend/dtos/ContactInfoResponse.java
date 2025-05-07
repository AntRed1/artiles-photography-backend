package com.artiles_photography_backend.dtos;

import lombok.Data;

/**
 *
 * @author arojas
 */
@Data
public class ContactInfoResponse {
	private Long id;
	private String phone;
	private String email;
	private String address;
	private String whatsapp;
	private String facebook;
	private String instagram;
	private String twitter;
	private String tiktok;
	private String googleMapsUrl;
}
