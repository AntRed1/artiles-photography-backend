
package com.artiles_photography_backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.artiles_photography_backend.dtos.AboutUsRequest;
import com.artiles_photography_backend.dtos.AboutUsResponse;
import com.artiles_photography_backend.models.AboutUs;
import com.artiles_photography_backend.repository.AboutUsRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

/**
 *
 * @author arojas
 *         * Servicio para manejar operaciones relacionadas con AboutUs.
 *
 */
@Service
public class AboutUsService {

    private final AboutUsRepository aboutUsRepository;

    public AboutUsService(AboutUsRepository aboutUsRepository) {
        this.aboutUsRepository = aboutUsRepository;
    }

    public AboutUsResponse getAboutUs() {
        AboutUs aboutUs = aboutUsRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new EntityNotFoundException("No se encontró información Sobre Nosotros"));
        return mapToResponse(aboutUs);
    }

    public AboutUsResponse getAboutUsById(Long id) {
        AboutUs aboutUs = aboutUsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Información no encontrada con ID: " + id));
        return mapToResponse(aboutUs);
    }

    public AboutUsResponse getAboutUsByTitle(String title) {
        AboutUs aboutUs = aboutUsRepository.findByTitle(title)
                .orElseThrow(() -> new EntityNotFoundException("Información no encontrada para el título: " + title));
        return mapToResponse(aboutUs);
    }

    public List<AboutUsResponse> getAllAboutUs() {
        return aboutUsRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AboutUsResponse createAboutUs(AboutUsRequest request) {
        AboutUs aboutUs = new AboutUs();
        updateEntityFromRequest(aboutUs, request);
        aboutUs = aboutUsRepository.save(aboutUs);
        return mapToResponse(aboutUs);
    }

    @Transactional
    public AboutUsResponse updateAboutUs(Long id, AboutUsRequest request) {
        AboutUs aboutUs = aboutUsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Información no encontrada con ID: " + id));
        updateEntityFromRequest(aboutUs, request);
        aboutUs = aboutUsRepository.save(aboutUs);
        return mapToResponse(aboutUs);
    }

    @Transactional
    public void deleteAboutUs(Long id) {
        if (!aboutUsRepository.existsById(id)) {
            throw new EntityNotFoundException("Información no encontrada con ID: " + id);
        }
        aboutUsRepository.deleteById(id);
    }

    private void updateEntityFromRequest(AboutUs aboutUs, AboutUsRequest request) {
        aboutUs.setTitle(request.getTitle());
        aboutUs.setContent(request.getContent());
        aboutUs.setSpecialties(request.getSpecialties());
        aboutUs.setSpecialtyIcons(request.getSpecialtyIcons());
    }

    private AboutUsResponse mapToResponse(AboutUs aboutUs) {
        AboutUsResponse response = new AboutUsResponse();
        response.setId(aboutUs.getId());
        response.setTitle(aboutUs.getTitle());
        response.setContent(aboutUs.getContent());
        response.setSpecialties(aboutUs.getSpecialties());
        response.setSpecialtyIcons(aboutUs.getSpecialtyIcons());
        return response;
    }
}