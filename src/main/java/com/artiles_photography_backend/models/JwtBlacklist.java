package com.artiles_photography_backend.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author arojas
 *         Entidad para almacenar tokens JWT en lista negra.
 */
@Entity
@Table(name = "jwt_blacklist", indexes = {
		@Index(name = "idx_token_hash", columnList = "tokenHash", unique = true)
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtBlacklist {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String token;

	@Column(nullable = false, length = 64, unique = true)
	private String tokenHash;

	@Column(nullable = false)
	private LocalDateTime expiryDate;
}