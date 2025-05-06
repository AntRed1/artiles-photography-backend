package com.artiles_photography_backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.artiles_photography_backend.models.Testimonial;
import com.artiles_photography_backend.repository.TestimonialRepository;
import com.artiles_photography_backend.services.TestimonialService;

import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author arojas
 *         * Controlador REST para manejar peticiones relacionadas con
 *         Testimonial.
 *
 */
@RestController
@RequestMapping("/api/testimonials")
public class TestimonialController {

	private final TestimonialService testimonialService;
	private final RestTemplate restTemplate;
	private final TestimonialRepository testimonialRepository;

	@Autowired
	public TestimonialController(TestimonialService testimonialService, RestTemplate restTemplate) {
		this.testimonialService = testimonialService;
		this.restTemplate = restTemplate;
		this.testimonialRepository = null;
	}

	/**
	 * Obtiene todos los testimonios habilitados.
	 */
	@GetMapping
	public ResponseEntity<List<Testimonial>> getTestimonials() {
		List<Testimonial> testimonials = testimonialService.getAllTestimonials();
		return ResponseEntity.ok(testimonials);
	}

	/**
	 * Guarda un nuevo testimonio.
	 */
	@PostMapping
	public ResponseEntity<Testimonial> saveTestimonial(
			@Validated @RequestBody Testimonial testimonial,
			HttpServletRequest request) {
		if (testimonial.getId() != null) {
			throw new IllegalArgumentException("El ID del testimonio debe ser nulo para nuevos registros");
		}
		testimonial.setCreatedAt(null); // Forzar que createdAt se genere en el servidor

		// Capturar IP
		String ipAddress = request.getHeader("X-Forwarded-For");
		if (ipAddress == null || ipAddress.isEmpty()) {
			ipAddress = request.getRemoteAddr();
		}
		testimonial.setIpAddress(ipAddress);

		// Obtener ubicación desde ip-api.com
		try {
			String url = "http://ip-api.com/json/" + ipAddress;
			String response = restTemplate.getForObject(url, String.class);
			if (response != null) {
				// Parsear respuesta manualmente
				String city = response.contains("\"city\":\"") ? response.split("\"city\":\"")[1].split("\"")[0] : "";
				String country = response.contains("\"country\":\"")
						? response.split("\"country\":\"")[1].split("\"")[0]
						: "";
				String location = city + (city.isEmpty() ? "" : ", ") + country;
				testimonial.setLocation(location.isEmpty() ? "Desconocida" : location);
			} else {
				testimonial.setLocation("Desconocida");
			}
		} catch (Exception e) {
			testimonial.setLocation("Desconocida");
		}

		return ResponseEntity.ok(testimonialService.saveTestimonial(testimonial));
	}

	@PatchMapping("/{id}/toggle-enable")
	public ResponseEntity<Testimonial> toggleEnable(@PathVariable Long id) {
		Testimonial testimonial = testimonialRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Testimonio no encontrado"));
		testimonial.setEnable(!testimonial.isEnable());
		return ResponseEntity.ok(testimonialRepository.save(testimonial));
	}
}