package com.artiles_photography_backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.artiles_photography_backend.dtos.ContactMessageRequest;
import com.artiles_photography_backend.dtos.ContactMessageResponse;
import com.artiles_photography_backend.models.ContactMessage;
import com.artiles_photography_backend.repository.ContactMessageRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

/**
 *
 * @author arojas
 */
@Service
public class ContactMessageService {

	private final ContactMessageRepository contactMessageRepository;

	public ContactMessageService(ContactMessageRepository contactMessageRepository) {
		this.contactMessageRepository = contactMessageRepository;
	}

	public List<ContactMessageResponse> getAllContactMessages() {
		return contactMessageRepository.findAll().stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public ContactMessageResponse getContactMessageById(Long id) {
		ContactMessage message = contactMessageRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Mensaje de contacto no encontrado con ID: " + id));
		return mapToResponse(message);
	}

	public List<ContactMessageResponse> getContactMessagesByEmail(String email) {
		return contactMessageRepository.findByEmail(email).stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public List<ContactMessageResponse> getContactMessagesByService(String service) {
		return contactMessageRepository.findByService(service).stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public ContactMessageResponse createContactMessage(ContactMessageRequest request, String clientIp,
			String userAgent) {
		ContactMessage message = new ContactMessage();
		updateEntityFromRequest(message, request);
		message.setClientIp(clientIp);
		message.setUserAgent(userAgent);
		message = contactMessageRepository.save(message);
		return mapToResponse(message);
	}

	@Transactional
	public ContactMessageResponse updateContactMessage(Long id, ContactMessageRequest request) {
		ContactMessage message = contactMessageRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Mensaje de contacto no encontrado con ID: " + id));
		updateEntityFromRequest(message, request);
		message = contactMessageRepository.save(message);
		return mapToResponse(message);
	}

	@Transactional
	public void deleteContactMessage(Long id) {
		if (!contactMessageRepository.existsById(id)) {
			throw new EntityNotFoundException("Mensaje de contacto no encontrado con ID: " + id);
		}
		contactMessageRepository.deleteById(id);
	}

	private void updateEntityFromRequest(ContactMessage message, ContactMessageRequest request) {
		message.setName(request.getName());
		message.setEmail(request.getEmail());
		message.setPhone(request.getPhone());
		message.setService(request.getService());
		message.setMessage(request.getMessage());
	}

	private ContactMessageResponse mapToResponse(ContactMessage message) {
		ContactMessageResponse response = new ContactMessageResponse();
		response.setId(message.getId());
		response.setName(message.getName());
		response.setEmail(message.getEmail());
		response.setPhone(message.getPhone());
		response.setService(message.getService());
		response.setMessage(message.getMessage());
		response.setClientIp(message.getClientIp());
		response.setUserAgent(message.getUserAgent());
		response.setCreatedAt(message.getCreatedAt());
		return response;
	}
}
