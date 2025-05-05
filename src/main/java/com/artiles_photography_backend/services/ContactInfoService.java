package com.artiles_photography_backend.services;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.ContactInfo;
import com.artiles_photography_backend.repository.ContactInfoRepository;

/**
 *
 * @author arojas
 */
@Service
public class ContactInfoService {

	private final ContactInfoRepository contactInfoRepository;

	@Autowired
	public ContactInfoService(ContactInfoRepository contactInfoRepository) {
		this.contactInfoRepository = contactInfoRepository;
	}

	/**
	 * Obtiene toda la información de contacto.
	 */
	public List<ContactInfo> getAllContactInfo() {
		return (List<ContactInfo>) contactInfoRepository.findAll();
	}

	/**
	 * Obtiene la primera información de contacto disponible.
	 */
	public ContactInfo getContactInfo() {
		List<ContactInfo> contactInfoList = (List<ContactInfo>) contactInfoRepository.findAll();
		if (contactInfoList.isEmpty()) {
			throw new NoSuchElementException("No se encontró información de contacto");
		}
		return contactInfoList.get(0);
	}

	/**
	 * Guarda la información de contacto en la base de datos.
	 */
	public ContactInfo saveContactInfo(ContactInfo contactInfo) {
		return contactInfoRepository.save(contactInfo);
	}
}