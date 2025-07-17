package com.artiles_photography_backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artiles_photography_backend.dtos.ContactMessageRequest;
import com.artiles_photography_backend.dtos.ContactMessageResponse;
import com.artiles_photography_backend.models.ContactMessage;
import com.artiles_photography_backend.repository.ContactMessageRepository;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;

/**
 * Service for managing contact messages.
 *
 * @author arojas
 */
@Service
public class ContactMessageService {
	private static final Logger logger = LoggerFactory.getLogger(ContactMessageService.class);

	private final ContactMessageRepository contactMessageRepository;
	private final EmailService emailService;

	public ContactMessageService(
			ContactMessageRepository contactMessageRepository,
			EmailService emailService) {
		this.contactMessageRepository = contactMessageRepository;
		this.emailService = emailService;
	}

	public List<ContactMessageResponse> getAllContactMessages() {
		logger.debug("Obteniendo todos los mensajes de contacto");
		return contactMessageRepository.findAll().stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public ContactMessageResponse getContactMessageById(Long id) {
		logger.debug("Obteniendo mensaje de contacto con ID: {}", id);
		ContactMessage message = contactMessageRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Mensaje de contacto no encontrado con ID: " + id));
		return mapToResponse(message);
	}

	public List<ContactMessageResponse> getContactMessagesByEmail(String email) {
		logger.debug("Obteniendo mensajes de contacto por email: {}", email);
		return contactMessageRepository.findByEmail(email).stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public List<ContactMessageResponse> getContactMessagesByService(String service) {
		logger.debug("Obteniendo mensajes de contacto por servicio: {}", service);
		return contactMessageRepository.findByService(service).stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public ContactMessageResponse createContactMessage(ContactMessageRequest request, String clientIp,
			String userAgent) {
		logger.info("Creando nuevo mensaje de contacto desde IP: {}, User-Agent: {}", clientIp, userAgent);
		ContactMessage message = new ContactMessage();
		updateEntityFromRequest(message, request);
		message.setClientIp(clientIp);
		message.setUserAgent(userAgent);
		message = contactMessageRepository.save(message);

		try {
			emailService.sendContactEmail(message);
			logger.info("Correos enviados para mensaje ID: {}", message.getId());
		} catch (MessagingException e) {
			logger.error("Error al enviar correos para mensaje ID: {}: {}", message.getId(), e.getMessage(), e);
		}

		return mapToResponse(message);
	}

	@Transactional
	public ContactMessageResponse updateContactMessage(Long id, ContactMessageRequest request) {
		logger.debug("Actualizando mensaje de contacto con ID: {}", id);
		ContactMessage message = contactMessageRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Mensaje de contacto no encontrado con ID: " + id));
		updateEntityFromRequest(message, request);
		message = contactMessageRepository.save(message);
		return mapToResponse(message);
	}

	@Transactional
	public void deleteContactMessage(Long id) {
		logger.debug("Eliminando mensaje de contacto con ID: {}", id);
		if (!contactMessageRepository.existsById(id)) {
			throw new EntityNotFoundException("Mensaje de contacto no encontrado con ID: " + id);
		}
		contactMessageRepository.deleteById(id);
		logger.info("Mensaje de contacto eliminado con ID: {}", id);
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