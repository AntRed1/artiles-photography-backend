package com.artiles_photography_backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.artiles_photography_backend.dtos.ContactInfoRequest;
import com.artiles_photography_backend.dtos.ContactInfoResponse;
import com.artiles_photography_backend.models.ContactInfo;
import com.artiles_photography_backend.repository.ContactInfoRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

/**
 *
 * @author arojas
 */
@Service
public class ContactInfoService {

	private final ContactInfoRepository contactInfoRepository;

	public ContactInfoService(ContactInfoRepository contactInfoRepository) {
		this.contactInfoRepository = contactInfoRepository;
	}

	public ContactInfoResponse getContactInfo() {
		List<ContactInfo> contactInfoList = contactInfoRepository.findAll();
		if (contactInfoList.isEmpty()) {
			return null;
		}
		return mapToResponse(contactInfoList.get(0));
	}

	public ContactInfoResponse getContactInfoById(Long id) {
		ContactInfo contactInfo = contactInfoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Información de contacto no encontrada con ID: " + id));
		return mapToResponse(contactInfo);
	}

	public List<ContactInfoResponse> getAllContactInfo() {
		return contactInfoRepository.findAll().stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public ContactInfoResponse createContactInfo(ContactInfoRequest request) {
		ContactInfo contactInfo = new ContactInfo();
		updateEntityFromRequest(contactInfo, request);
		contactInfo = contactInfoRepository.save(contactInfo);
		return mapToResponse(contactInfo);
	}

	@Transactional
	public ContactInfoResponse updateContactInfo(Long id, ContactInfoRequest request) {
		ContactInfo contactInfo = contactInfoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Información de contacto no encontrada con ID: " + id));
		updateEntityFromRequest(contactInfo, request);
		contactInfo = contactInfoRepository.save(contactInfo);
		return mapToResponse(contactInfo);
	}

	@Transactional
	public void deleteContactInfo(Long id) {
		if (!contactInfoRepository.existsById(id)) {
			throw new EntityNotFoundException("Información de contacto no encontrada con ID: " + id);
		}
		contactInfoRepository.deleteById(id);
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