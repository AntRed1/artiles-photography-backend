package com.artiles_photography_backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.artiles_photography_backend.dtos.LegalRequest;
import com.artiles_photography_backend.dtos.LegalResponse;
import com.artiles_photography_backend.models.Legal;
import com.artiles_photography_backend.repository.LegalRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

/**
 *
 * @author arojas
 *         * Servicio para manejar operaciones relacionadas con Legal.
 *
 */
@Service
public class LegalService {

	private final LegalRepository legalRepository;

	public LegalService(LegalRepository legalRepository) {
		this.legalRepository = legalRepository;
	}

	public List<LegalResponse> getAllLegals() {
		return legalRepository.findAll().stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public LegalResponse getLegalById(Long id) {
		Legal legal = legalRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Documento legal no encontrado con ID: " + id));
		return mapToResponse(legal);
	}

	public LegalResponse getLegalByType(String type) {
		Legal legal = legalRepository.findByType(type)
				.orElseThrow(() -> new EntityNotFoundException("Documento legal no encontrado con tipo: " + type));
		return mapToResponse(legal);
	}

	@Transactional
	public LegalResponse createLegal(LegalRequest request) {
		Legal legal = new Legal();
		updateEntityFromRequest(legal, request);
		legal = legalRepository.save(legal);
		return mapToResponse(legal);
	}

	@Transactional
	public LegalResponse updateLegal(Long id, LegalRequest request) {
		Legal legal = legalRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Documento legal no encontrado con ID: " + id));
		updateEntityFromRequest(legal, request);
		legal = legalRepository.save(legal);
		return mapToResponse(legal);
	}

	@Transactional
	public void deleteLegal(Long id) {
		if (!legalRepository.existsById(id)) {
			throw new EntityNotFoundException("Documento legal no encontrado con ID: " + id);
		}
		legalRepository.deleteById(id);
	}

	private void updateEntityFromRequest(Legal legal, LegalRequest request) {
		legal.setType(request.getType());
		legal.setContent(request.getContent());
	}

	private LegalResponse mapToResponse(Legal legal) {
		LegalResponse response = new LegalResponse();
		response.setId(legal.getId());
		response.setType(legal.getType());
		response.setContent(legal.getContent());
		return response;
	}
}