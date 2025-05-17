package com.artiles_photography_backend.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.models.HttpLog;
import com.artiles_photography_backend.repository.HttpLogRepository;

/**
 *
 * @author arojas
 */
@RestController
@RequestMapping("/api/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
public class HttpLogController {

	@Autowired
	private HttpLogRepository httpLogRepository;

	// Listar todos los logs con paginación
	@GetMapping
	public ResponseEntity<Map<String, Object>> getLogs(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "timestamp,desc") String sort) {
		try {
			// Configurar ordenación
			String[] sortParams = sort.split(",");
			Sort.Order order = new Sort.Order(
					sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
					sortParams[0]);
			Pageable pageable = PageRequest.of(page, size, Sort.by(order));

			// Obtener logs
			Page<HttpLog> logPage = httpLogRepository.findAll(pageable);

			// Construir respuesta
			Map<String, Object> response = new HashMap<>();
			response.put("logs", logPage.getContent());
			response.put("currentPage", logPage.getNumber());
			response.put("totalItems", logPage.getTotalElements());
			response.put("totalPages", logPage.getTotalPages());

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", "Error al obtener los logs: " + e.getMessage()));
		}
	}

	// Obtener un log por ID
	@GetMapping("/{id}")
	public ResponseEntity<?> getLogById(@PathVariable Long id) {
		Optional<HttpLog> log = httpLogRepository.findById(id);
		if (log.isPresent()) {
			return ResponseEntity.ok(log.get());
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// Filtrar logs por método, URI, estado, y rango de fechas
	@GetMapping("/filter")
	public ResponseEntity<Map<String, Object>> filterLogs(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "timestamp,desc") String sort,
			@RequestParam(required = false) String method,
			@RequestParam(required = false) String uri,
			@RequestParam(required = false) Integer status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
		try {
			// Configurar ordenación
			String[] sortParams = sort.split(",");
			Sort.Order order = new Sort.Order(
					sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
					sortParams[0]);
			Pageable pageable = PageRequest.of(page, size, Sort.by(order));

			// Obtener logs filtrados
			Page<HttpLog> logPage = httpLogRepository.findLogsByFilters(method, uri, status, startDate, endDate,
					pageable);

			// Construir respuesta
			Map<String, Object> response = new HashMap<>();
			response.put("logs", logPage.getContent());
			response.put("currentPage", logPage.getNumber());
			response.put("totalItems", logPage.getTotalElements());
			response.put("totalPages", logPage.getTotalPages());

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", "Error al filtrar los logs: " + e.getMessage()));
		}
	}

	// Eliminar logs anteriores a una fecha
	@DeleteMapping("/before")
	public ResponseEntity<Map<String, String>> deleteLogsBefore(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
		try {
			httpLogRepository.deleteLogsBefore(date);
			return ResponseEntity.ok(Map.of("message", "Logs anteriores a " + date + " eliminados correctamente"));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", "Error al eliminar los logs: " + e.getMessage()));
		}
	}
}