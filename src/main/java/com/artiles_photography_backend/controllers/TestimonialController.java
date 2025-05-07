package com.artiles_photography_backend.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.dtos.TestimonialRequest;
import com.artiles_photography_backend.dtos.TestimonialResponse;
import com.artiles_photography_backend.services.TestimonialService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * @author arojas
 *         Controlador REST para manejar peticiones relacionadas con
 *         Testimonial.
 */
@RestController
@RequestMapping("/api/testimonials")
public class TestimonialController {

	private final TestimonialService testimonialService;

	public TestimonialController(TestimonialService testimonialService) {
		this.testimonialService = testimonialService;
	}

	@GetMapping
	public ResponseEntity<List<TestimonialResponse>> getTestimonials() {
		return ResponseEntity.ok(testimonialService.getAllTestimonials());
	}

	@GetMapping("/all")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<TestimonialResponse>> getAllTestimonialsAdmin() {
		return ResponseEntity.ok(testimonialService.getAllTestimonialsAdmin());
	}

	@GetMapping("/{id}")
	public ResponseEntity<TestimonialResponse> getTestimonialById(@PathVariable Long id) {
		return ResponseEntity.ok(testimonialService.getTestimonialById(id));
	}

	@PostMapping
	public ResponseEntity<TestimonialResponse> createTestimonial(
			@Valid @RequestBody TestimonialRequest request, HttpServletRequest httpRequest) {
		return ResponseEntity.status(201).body(testimonialService.createTestimonial(request, httpRequest));
	}

	@PutMapping("/admin/testimonials/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<TestimonialResponse> updateTestimonial(
			@PathVariable Long id, @Valid @RequestBody TestimonialRequest request) {
		return ResponseEntity.ok(testimonialService.updateTestimonial(id, request));
	}

	@PatchMapping("/admin/testimonials/{id}/toggle-enable")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<TestimonialResponse> toggleEnable(@PathVariable Long id) {
		return ResponseEntity.ok(testimonialService.toggleEnable(id));
	}

	@DeleteMapping("/admin/testimonials/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteTestimonial(@PathVariable Long id) {
		testimonialService.deleteTestimonial(id);
		return ResponseEntity.noContent().build();
	}
}