package com.artiles_photography_backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.artiles_photography_backend.dtos.TestimonialRequest;
import com.artiles_photography_backend.dtos.TestimonialResponse;
import com.artiles_photography_backend.models.Testimonial;
import com.artiles_photography_backend.repository.TestimonialRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

/**
 *
 * @author arojas
 *         * Servicio para manejar operaciones relacionadas con Testimonial.
 *
 */
@Service
public class TestimonialService {

    private final TestimonialRepository testimonialRepository;
    private final RestTemplate restTemplate;

    public TestimonialService(TestimonialRepository testimonialRepository, RestTemplate restTemplate) {
        this.testimonialRepository = testimonialRepository;
        this.restTemplate = restTemplate;
    }

    public List<TestimonialResponse> getAllTestimonials() {
        return testimonialRepository.findByEnableTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TestimonialResponse> getAllTestimonialsAdmin() {
        return testimonialRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TestimonialResponse getTestimonialById(Long id) {
        Testimonial testimonial = testimonialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Testimonio no encontrado con ID: " + id));
        return mapToResponse(testimonial);
    }

    @Transactional
    public TestimonialResponse createTestimonial(TestimonialRequest request, HttpServletRequest httpRequest) {
        Testimonial testimonial = new Testimonial();
        updateEntityFromRequest(testimonial, request);

        // Capturar IP
        String ipAddress = httpRequest.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = httpRequest.getRemoteAddr();
        }
        testimonial.setIpAddress(ipAddress);

        // Capturar dispositivo desde User-Agent
        String userAgent = httpRequest.getHeader("User-Agent");
        testimonial.setDevice(userAgent != null && !userAgent.isEmpty() ? userAgent : "Desconocido");

        // Obtener ubicación desde ip-api.com
        try {
            String url = "http://ip-api.com/json/" + ipAddress;
            String response = restTemplate.getForObject(url, String.class);
            if (response != null) {
                String city = response.contains("\"city\":\"") ? response.split("\"city\":\"")[1].split("\"")[0] : "";
                String country = response.contains("\"country\":\"")
                        ? response.split("\"country\":\"")[1].split("\"")[0]
                        : "";
                String location = city + (city.isEmpty() ? "" : ", ") + country;
                testimonial.setLocation(location.isEmpty() ? "Desconocida" : location);
            } else {
                testimonial.setLocation("Desconocida");
            }
        } catch (RestClientException e) {
            testimonial.setLocation("Desconocida");
        }

        testimonial = testimonialRepository.save(testimonial);
        return mapToResponse(testimonial);
    }

    @Transactional
    public TestimonialResponse updateTestimonial(Long id, TestimonialRequest request) {
        Testimonial testimonial = testimonialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Testimonio no encontrado con ID: " + id));
        updateEntityFromRequest(testimonial, request);
        testimonial = testimonialRepository.save(testimonial);
        return mapToResponse(testimonial);
    }

    @Transactional
    public TestimonialResponse toggleEnable(Long id) {
        Testimonial testimonial = testimonialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Testimonio no encontrado con ID: " + id));
        testimonial.setEnable(!testimonial.isEnable());
        testimonial = testimonialRepository.save(testimonial);
        return mapToResponse(testimonial);
    }

    @Transactional
    public void deleteTestimonial(Long id) {
        if (!testimonialRepository.existsById(id)) {
            throw new EntityNotFoundException("Testimonio no encontrado con ID: " + id);
        }
        testimonialRepository.deleteById(id);
    }

    private void updateEntityFromRequest(Testimonial testimonial, TestimonialRequest request) {
        testimonial.setName(request.getName());
        testimonial.setRating(request.getRating());
        testimonial.setMessage(request.getMessage());
        testimonial.setEnable(request.isEnable());
    }

    private TestimonialResponse mapToResponse(Testimonial testimonial) {
        TestimonialResponse response = new TestimonialResponse();
        response.setId(testimonial.getId());
        response.setName(testimonial.getName());
        response.setRating(testimonial.getRating());
        response.setMessage(testimonial.getMessage());
        response.setCreatedAt(testimonial.getCreatedAt());
        response.setDevice(testimonial.getDevice());
        response.setIpAddress(testimonial.getIpAddress());
        response.setLocation(testimonial.getLocation());
        response.setEnable(testimonial.isEnable());
        return response;
    }
}
