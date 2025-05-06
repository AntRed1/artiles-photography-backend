package com.artiles_photography_backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.models.Testimonial;
import com.artiles_photography_backend.services.TestimonialService;

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

	@Autowired
	public TestimonialController(TestimonialService testimonialService) {
		this.testimonialService = testimonialService;
	}

	/**
	 * Obtiene todos los testimonios.
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
	public ResponseEntity<Testimonial> saveTestimonial(@RequestBody Testimonial testimonial) {
		return ResponseEntity.ok(testimonialService.saveTestimonial(testimonial));
	}
}
