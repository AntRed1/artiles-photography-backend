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
 */
@RestController
@RequestMapping("/api/testimonials")
public class TestimonialController {
	@Autowired
	private TestimonialService testimonialService;

	@GetMapping
	public ResponseEntity<List<Testimonial>> getAllTestimonials() {
		return ResponseEntity.ok(testimonialService.getAllTestimonials());
	}

	@PostMapping
	public ResponseEntity<Testimonial> addTestimonial(@RequestBody Testimonial testimonial) {
		return ResponseEntity.ok(testimonialService.addTestimonial(testimonial));
	}
}
