package com.artiles_photography_backend.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author arojas
 */
@Entity
@Table(name = "http_logs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String method;

	@Column(nullable = false)
	private String uri;

	@Column
	private String queryString;

	@Column(columnDefinition = "TEXT")
	private String requestBody;

	@Column(nullable = false)
	private int status;

	@Column
	private String contentType;

	@Column(columnDefinition = "TEXT")
	private String responseBody;

	@Column(nullable = false)
	private LocalDateTime timestamp;

	public HttpLog(String method, String uri, String queryString, String requestBody,
			int status, String contentType, String responseBody) {
		this.method = method;
		this.uri = uri;
		this.queryString = queryString;
		this.requestBody = requestBody;
		this.status = status;
		this.contentType = contentType;
		this.responseBody = responseBody;
		this.timestamp = LocalDateTime.now();
	}
}
