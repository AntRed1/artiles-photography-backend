package com.artiles_photography_backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.dtos.ContactInfoRequest;
import com.artiles_photography_backend.dtos.ContactInfoResponse;
import com.artiles_photography_backend.models.ContactInfo;
import com.artiles_photography_backend.repository.ContactInfoRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

/**
 * @author arojas
 */
@Service
public class ContactInfoService {

	private static final Logger logger = LoggerFactory.getLogger(ContactInfoService.class);

	private final ContactInfoRepository contactInfoRepository;

	public ContactInfoService(ContactInfoRepository contactInfoRepository) {
		this.contactInfoRepository = contactInfoRepository;
	}

	public ContactInfoResponse getContactInfo() {
		logger.debug("Obteniendo información de contacto");
		List<ContactInfo> contactInfoList = contactInfoRepository.findAll();
		if (contactInfoList.isEmpty()) {
			logger.warn("No se encontró información de contacto");
			return null;
		}
		return mapToResponse(contactInfoList.get(0));
	}

	public ContactInfoResponse getContactInfoById(Long id) {
		logger.debug("Obteniendo información de contacto con ID: {}", id);
		ContactInfo contactInfo = contactInfoRepository.findById(id)
				.orElseThrow(() -> {
					logger.warn("Información de contacto no encontrada con ID: {}", id);
					return new EntityNotFoundException("Información de contacto no encontrada con ID: " + id);
				});
		return mapToResponse(contactInfo);
	}

	public List<ContactInfoResponse> getAllContactInfo() {
		logger.debug("Obteniendo todas las informaciones de contacto");
		return contactInfoRepository.findAll().stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public ContactInfoResponse createContactInfo(ContactInfoRequest request) {
		logger.debug("Creando nueva información de contacto: {}", request);
		ContactInfo contactInfo = new ContactInfo();
		updateEntityFromRequest(contactInfo, request);
		contactInfo = contactInfoRepository.save(contactInfo);
		logger.info("Información de contacto creada con ID: {}", contactInfo.getId());
		return mapToResponse(contactInfo);
	}

	@Transactional
	public ContactInfoResponse updateContactInfo(Long id, ContactInfoRequest request) {
		logger.debug("Actualizando información de contacto con ID: {}, datos: {}", id, request);
		ContactInfo contactInfo = contactInfoRepository.findById(id)
				.orElseThrow(() -> {
					logger.warn("Información de contacto no encontrada con ID: {}", id);
					return new EntityNotFoundException("Información de contacto no encontrada con ID: " + id);
				});
		updateEntityFromRequest(contactInfo, request);
		contactInfo = contactInfoRepository.save(contactInfo);
		logger.info("Información de contacto actualizada con ID: {}", id);
		return mapToResponse(contactInfo);
	}

	@Transactional
	public void deleteContactInfo(Long id) {
		logger.debug("Eliminando información de contacto con ID: {}", id);
		if (!contactInfoRepository.existsById(id)) {
			logger.warn("Información de contacto no encontrada con ID: {}", id);
			throw new EntityNotFoundException("Información de contacto no encontrada con ID: " + id);
		}
		contactInfoRepository.deleteById(id);
		logger.info("Información de contacto eliminada con ID: {}", id);
	}

	private void updateEntityFromRequest(ContactInfo contactInfo, ContactInfoRequest request) {
		contactInfo.setPhone(request.getPhone());
		contactInfo.setEmail(request.getEmail());
		contactInfo.setAddress(request.getAddress());
		contactInfo.setWhatsapp(request.getWhatsapp());
		contactInfo.setFacebook(request.getFacebook());
		contactInfo.setInstagram(request.getInstagram());
		contactInfo.setTwitter(request.getTwitter());
		contactInfo.setTiktok(request.getTiktok());
		contactInfo.setGoogleMapsUrl(request.getGoogleMapsUrl());
	}

	private ContactInfoResponse mapToResponse(ContactInfo contactInfo) {
		ContactInfoResponse response = new ContactInfoResponse();
		response.setId(contactInfo.getId());
		response.setPhone(contactInfo.getPhone());
		response.setEmail(contactInfo.getEmail());
		response.setAddress(contactInfo.getAddress());
		response.setWhatsapp(contactInfo.getWhatsapp());
		response.setFacebook(contactInfo.getFacebook());
		response.setInstagram(contactInfo.getInstagram());
		response.setTwitter(contactInfo.getTwitter());
		response.setTiktok(contactInfo.getTiktok());
		response.setGoogleMapsUrl(contactInfo.getGoogleMapsUrl());
		return response;
	}
}