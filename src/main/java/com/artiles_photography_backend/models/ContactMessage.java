package com.artiles_photography_backend.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author arojas
 */
@Entity
@Table(name = "contact_messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false)
	private String name;

	@NotBlank
	@Email
	@Size(max = 255)
	@Column(nullable = false)
	private String email;

	@Size(max = 20)
	@Column
	private String phone;

	@Size(max = 100)
	@Column
	private String service;

	@NotBlank
	@Size(max = 2000)
	@Column(columnDefinition = "TEXT", nullable = false)
	private String message;

	@Size(max = 45)
	@Column
	private String clientIp;

	@Size(max = 255)
	@Column
	private String userAgent;

	@Column(nullable = false)
	@org.hibernate.annotations.CreationTimestamp
	private LocalDateTime createdAt = LocalDateTime.now();
}
