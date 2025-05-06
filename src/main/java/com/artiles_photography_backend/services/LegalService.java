package com.artiles_photography_backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.Legal;
import com.artiles_photography_backend.repository.LegalRepository;

/**
 *
 * @author arojas
 *         * Servicio para manejar operaciones relacionadas con Legal.
 * 
 */
@Service
public class LegalService {

	private final LegalRepository legalRepository;

	@Autowired
	public LegalService(LegalRepository legalRepository) {
		this.legalRepository = legalRepository;
	}

	/**
	 * Obtiene todos los documentos legales.
	 */
	public List<Legal> getAllLegals() {
		return (List<Legal>) legalRepository.findAll();
	}

	/**
	 * Guarda un documento legal.
	 */
	public Legal saveLegal(Legal legal) {
		return legalRepository.save(legal);
	}
}
