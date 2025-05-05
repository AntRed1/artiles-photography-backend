package com.artiles_photography_backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.Legal;
import com.artiles_photography_backend.repository.LegalRepository;

/**
 *
 * @author arojas
 */
@Service
public class LegalService {
	@Autowired
	private LegalRepository legalRepository;

	public List<Legal> getLegalDocuments() {
		return legalRepository.findAll();
	}

	public Legal saveLegalDocument(Legal legal) {
		return legalRepository.save(legal);
	}
}
