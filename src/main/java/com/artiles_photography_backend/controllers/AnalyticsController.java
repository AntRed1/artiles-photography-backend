package com.artiles_photography_backend.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.artiles_photography_backend.models.Notification;
import com.artiles_photography_backend.repository.NotificationRepository;

/**
 * @author arojas
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

	private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

	@Value("${plausible.api.url}")
	private String plausibleApiUrl;

	@Value("${plausible.api.key}")
	private String plausibleApiKey;

	@Value("${plausible.domain}")
	private String plausibleDomain;

	private final RestTemplate restTemplate;

	@Autowired
	private NotificationRepository notificationRepository;

	private static final List<String> VALID_PERIODS = Arrays.asList(
			"12mo", "6mo", "month", "30d", "7d", "day", "custom", "6h", "12h", "24h");

	public AnalyticsController(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@GetMapping("/notifications")
	public ResponseEntity<List<Notification>> getNotifications() {
		String requestId = UUID.randomUUID().toString();
		MDC.put("requestId", requestId);
		try {
			List<Notification> notifications = notificationRepository.findTop10ByOrderByCreatedAtDesc();
			logger.debug("Returning notifications: {}", notifications);
			return ResponseEntity.ok(notifications);
		} catch (Exception e) {
			logger.error("Error fetching notifications: {}", e.getMessage(), e);
			return ResponseEntity.status(500).body(null);
		} finally {
			MDC.clear();
		}
	}

	@GetMapping("/stats")
	public ResponseEntity<?> getStats(@RequestParam(defaultValue = "day") String period) {
		if (!VALID_PERIODS.contains(period)) {
			return ResponseEntity.badRequest()
					.body("Período inválido: " + period + ". Valores permitidos: " + VALID_PERIODS);
		}
		String url = plausibleApiUrl + "/stats/aggregate?site_id=" + plausibleDomain + "&period=" + period;
		return ResponseEntity.ok(fetchFromPlausible(url, HttpMethod.GET));
	}

	@GetMapping("/timeseries")
	public ResponseEntity<?> getTimeseries(@RequestParam(defaultValue = "day") String period) {
		if (!VALID_PERIODS.contains(period)) {
			return ResponseEntity.badRequest()
					.body("Período inválido: " + period + ". Valores permitidos: " + VALID_PERIODS);
		}
		String url = plausibleApiUrl + "/stats/timeseries?site_id=" + plausibleDomain + "&period=" + period;
		return ResponseEntity.ok(fetchFromPlausible(url, HttpMethod.GET));
	}

	@GetMapping("/recent-activity")
	public ResponseEntity<?> getRecentActivity(@RequestParam(defaultValue = "day") String period) {
		if (!VALID_PERIODS.contains(period)) {
			return ResponseEntity.badRequest()
					.body("Período inválido: " + period + ". Valores permitidos: " + VALID_PERIODS);
		}
		String url = plausibleApiUrl + "/stats/breakdown?site_id=" + plausibleDomain + "&period=" + period
				+ "&property=event:page";
		return ResponseEntity.ok(fetchFromPlausible(url, HttpMethod.GET));
	}

	@GetMapping("/events")
	public ResponseEntity<?> getEvents(@RequestParam(defaultValue = "day") String period) {
		if (!VALID_PERIODS.contains(period)) {
			return ResponseEntity.badRequest()
					.body("Período inválido: " + period + ". Valores permitidos: " + VALID_PERIODS);
		}
		String url = plausibleApiUrl + "/stats/breakdown?site_id=" + plausibleDomain + "&period=" + period
				+ "&property=event:name";
		return ResponseEntity.ok(fetchFromPlausible(url, HttpMethod.GET));
	}

	@GetMapping("/browsers")
	public ResponseEntity<?> getBrowsers(@RequestParam(defaultValue = "day") String period) {
		if (!VALID_PERIODS.contains(period)) {
			return ResponseEntity.badRequest()
					.body("Período inválido: " + period + ". Valores permitidos: " + VALID_PERIODS);
		}
		String url = plausibleApiUrl + "/stats/breakdown?site_id=" + plausibleDomain + "&period=" + period
				+ "&property=visit:browser";
		return ResponseEntity.ok(fetchFromPlausible(url, HttpMethod.GET));
	}

	@GetMapping("/cities")
	public ResponseEntity<?> getCities(@RequestParam(defaultValue = "day") String period) {
		if (!VALID_PERIODS.contains(period)) {
			return ResponseEntity.badRequest()
					.body("Período inválido: " + period + ". Valores permitidos: " + VALID_PERIODS);
		}
		String url = plausibleApiUrl + "/stats/breakdown?site_id=" + plausibleDomain + "&period=" + period
				+ "&property=visit:city";
		return ResponseEntity.ok(fetchFromPlausible(url, HttpMethod.GET));
	}

	@GetMapping("/sources")
	public ResponseEntity<?> getSources(@RequestParam(defaultValue = "day") String period) {
		if (!VALID_PERIODS.contains(period)) {
			return ResponseEntity.badRequest()
					.body("Período inválido: " + period + ". Valores permitidos: " + VALID_PERIODS);
		}
		String url = plausibleApiUrl + "/stats/breakdown?site_id=" + plausibleDomain + "&period=" + period
				+ "&property=visit:source";
		return ResponseEntity.ok(fetchFromPlausible(url, HttpMethod.GET));
	}

	@GetMapping("/devices")
	public ResponseEntity<?> getDevices(@RequestParam(defaultValue = "day") String period) {
		if (!VALID_PERIODS.contains(period)) {
			return ResponseEntity.badRequest()
					.body("Período inválido: " + period + ". Valores permitidos: " + VALID_PERIODS);
		}
		String url = plausibleApiUrl + "/stats/breakdown?site_id=" + plausibleDomain + "&period=" + period
				+ "&property=visit:device";
		return ResponseEntity.ok(fetchFromPlausible(url, HttpMethod.GET));
	}

	@PostMapping("/track-event")
	public ResponseEntity<?> trackEvent(@RequestBody PlausibleEvent event) {
		String requestId = UUID.randomUUID().toString();
		MDC.put("requestId", requestId);
		try {
			String sanitizedName = Encode.forJava(event.name());
			String sanitizedUrl = Encode.forJava(event.url());
			String sanitizedUserAgent = Encode.forJava(event.userAgent());
			String sanitizedIpAddress = Encode.forJava(event.ipAddress());
			String sanitizedProps = event.props() != null ? Encode.forJava(event.props()) : "{}";

			String url = plausibleApiUrl + "/event";
			HttpHeaders headers = new HttpHeaders();
			headers.set("User-Agent", sanitizedUserAgent);
			headers.set("X-Forwarded-For", sanitizedIpAddress);
			headers.set("Content-Type", "application/json");

			String payload = String.format(
					"{\"name\":\"%s\",\"url\":\"%s\",\"domain\":\"%s\",\"props\":%s}",
					sanitizedName,
					sanitizedUrl,
					plausibleDomain,
					sanitizedProps);

			HttpEntity<String> entity = new HttpEntity<>(payload, headers);
			logger.debug("Enviando evento a Plausible: {}", payload);
			restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			return ResponseEntity.ok().build();
		} catch (ResourceAccessException e) {
			logger.error("Error de I/O al conectar con Plausible: {}", e.getMessage(), e);
			return ResponseEntity.status(503).body("No se pudo conectar con Plausible");
		} catch (Exception e) {
			logger.error("Error al enviar evento: {}", e.getMessage(), e);
			return ResponseEntity.status(500).body("Error al enviar evento");
		} finally {
			MDC.clear();
		}
	}

	private Object fetchFromPlausible(String url, HttpMethod method) {
		String requestId = UUID.randomUUID().toString();
		MDC.put("requestId", requestId);
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer " + plausibleApiKey);
			HttpEntity<String> entity = new HttpEntity<>(headers);
			logger.debug("Haciendo solicitud a Plausible: {}", url);
			Object response = restTemplate.exchange(url, method, entity, Object.class).getBody();
			logger.debug("Respuesta de Plausible: {}", response);
			return response;
		} catch (ResourceAccessException e) {
			logger.error("Error de I/O al conectar con Plausible: {}", e.getMessage(), e);
			throw new RuntimeException("No se pudo conectar con Plausible: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Error inesperado al obtener datos: {}", e.getMessage(), e);
			throw new RuntimeException("Error al obtener datos: " + e.getMessage());
		} finally {
			MDC.clear();
		}
	}
}

record PlausibleEvent(String name, String url, String userAgent, String ipAddress, String props) {
	PlausibleEvent {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre del evento no puede ser nulo o vacío");
		}
		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("La URL no puede ser nula o vacía");
		}
	}
}