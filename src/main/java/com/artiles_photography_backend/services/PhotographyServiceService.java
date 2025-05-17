package com.artiles_photography_backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.dtos.PhotographyServiceRequest;
import com.artiles_photography_backend.dtos.PhotographyServiceResponse;
import com.artiles_photography_backend.models.PhotographyService;
import com.artiles_photography_backend.repository.PhotographyServiceRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

/**
 *
 * @author arojas
 *         * Servicio para manejar operaciones relacionadas con
 *         PhotographyService.
 *
 */
@Service
public class PhotographyServiceService {

	private final PhotographyServiceRepository photographyServiceRepository;

	public PhotographyServiceService(PhotographyServiceRepository photographyServiceRepository) {
		this.photographyServiceRepository = photographyServiceRepository;
	}

	public List<PhotographyServiceResponse> getAllPhotographyServices() {
		return photographyServiceRepository.findAll().stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public PhotographyServiceResponse getPhotographyServiceById(Long id) {
		PhotographyService service = photographyServiceRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Servicio fotográfico no encontrado con ID: " + id));
		return mapToResponse(service);
	}

	@Transactional
	public PhotographyServiceResponse createPhotographyService(PhotographyServiceRequest request) {
		// Verificar unicidad del título
		if (photographyServiceRepository.findByTitle(request.getTitle()).isPresent()) {
			throw new DataIntegrityViolationException("El título '" + request.getTitle() + "' ya está en uso");
		}

		PhotographyService service = new PhotographyService();
		updateEntityFromRequest(service, request);
		service = photographyServiceRepository.save(service);
		return mapToResponse(service);
	}

	@Transactional
	public PhotographyServiceResponse updatePhotographyService(Long id, PhotographyServiceRequest request) {
		PhotographyService service = photographyServiceRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Servicio fotográfico no encontrado con ID: " + id));

		// Verificar unicidad del título (excepto para el propio servicio)
		photographyServiceRepository.findByTitle(request.getTitle())
				.filter(existing -> !existing.getId().equals(id))
				.ifPresent(existing -> {
					throw new DataIntegrityViolationException("El título '" + request.getTitle() + "' ya está en uso");
				});

		updateEntityFromRequest(service, request);
		service = photographyServiceRepository.save(service);
		return mapToResponse(service);
	}

	@Transactional
	public void deletePhotographyService(Long id) {
		if (!photographyServiceRepository.existsById(id)) {
			throw new EntityNotFoundException("Servicio fotográfico no encontrado con ID: " + id);
		}
		photographyServiceRepository.deleteById(id);
	}

	private void updateEntityFromRequest(PhotographyService service, PhotographyServiceRequest request) {
		service.setTitle(request.getTitle());
		service.setIcon(request.getIcon());
	}

	private PhotographyServiceResponse mapToResponse(PhotographyService service) {
		PhotographyServiceResponse response = new PhotographyServiceResponse();
		response.setId(service.getId());
		response.setTitle(service.getTitle());
		response.setIcon(service.getIcon());
		return response;
	}
}
