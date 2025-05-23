package com.artiles_photography_backend.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.artiles_photography_backend.dtos.GalleryResponse;
import com.artiles_photography_backend.dtos.GalleryImageUpdateRequest;
import com.artiles_photography_backend.dtos.GalleryUploadRequest;
import com.artiles_photography_backend.exceptions.CloudinaryUploadException;
import com.artiles_photography_backend.models.Gallery;
import com.artiles_photography_backend.repository.GalleryRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

/**
 * @author arojas
 *         Servicio para manejar operaciones relacionadas con imágenes de la
 *         galería, incluyendo subida y eliminación en Cloudinary.
 */
@Service
public class GalleryService {

	private static final Logger logger = LoggerFactory.getLogger(GalleryService.class);
	private static final String CLOUDINARY_FOLDER = "photoquince/galeria";
	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
	private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png");

	private final GalleryRepository galleryRepository;
	private final Cloudinary cloudinary;

	public GalleryService(GalleryRepository galleryRepository, Cloudinary cloudinary) {
		this.galleryRepository = galleryRepository;
		this.cloudinary = cloudinary;
	}

	public List<GalleryResponse> getAllGalleryImages() {
		logger.debug("Obteniendo todas las imágenes de la galería ordenadas por uploadedAt");
		return galleryRepository.findAllByOrderByUploadedAtDesc().stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public GalleryResponse getGalleryImageById(Long id) {
		logger.debug("Obteniendo imagen de la galería con ID: {}", id);
		Gallery gallery = galleryRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Imagen de la galería no encontrada con ID: " + id));
		return mapToResponse(gallery);
	}

	@Transactional
	public GalleryResponse createGalleryImage(GalleryUploadRequest request) {
		logger.info("Subiendo nueva imagen de la galería con descripción: {}", request.getDescription());
		validateFile(request.getFile());
		try {
			Map uploadResult = cloudinary.uploader().upload(request.getFile().getBytes(),
					ObjectUtils.asMap("folder", CLOUDINARY_FOLDER));
			String url = (String) uploadResult.get("secure_url");
			String publicId = (String) uploadResult.get("public_id");
			Gallery gallery = new Gallery();
			gallery.setImageUrl(url);
			gallery.setDescription(request.getDescription());
			gallery.setUploadedAt(LocalDateTime.now());
			gallery = galleryRepository.save(gallery);
			logger.info("Imagen subida exitosamente a Cloudinary con public_id: {}", publicId);
			return mapToResponse(gallery);
		} catch (IOException e) {
			logger.error("Error al subir imagen a Cloudinary: {}", e.getMessage());
			throw new CloudinaryUploadException("Error al subir la imagen a Cloudinary", e);
		}
	}

	@Transactional
	public GalleryResponse updateGalleryImage(Long id, GalleryImageUpdateRequest request) {
		logger.info("Actualizando imagen de la galería con ID: {}", id);
		Gallery gallery = galleryRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Imagen de la galería no encontrada con ID: " + id));

		String oldPublicId = null;
		if (request.getFile() != null && !request.getFile().isEmpty()) {
			validateFile(request.getFile());
			try {
				// Guardar el public_id de la imagen anterior para eliminarla después
				oldPublicId = extractPublicId(gallery.getImageUrl());
				// Subir la nueva imagen a Cloudinary
				Map uploadResult = cloudinary.uploader().upload(request.getFile().getBytes(),
						ObjectUtils.asMap("folder", CLOUDINARY_FOLDER));
				gallery.setImageUrl((String) uploadResult.get("secure_url"));
				logger.info("Nueva imagen subida a Cloudinary con public_id: {}", uploadResult.get("public_id"));
			} catch (IOException e) {
				logger.error("Error al subir nueva imagen a Cloudinary: {}", e.getMessage());
				throw new CloudinaryUploadException("Error al subir la nueva imagen a Cloudinary", e);
			}
		}

		// Actualizar metadatos
		updateEntityFromRequest(gallery, request);
		gallery = galleryRepository.save(gallery);

		// Eliminar la imagen anterior de Cloudinary si se subió una nueva
		if (oldPublicId != null) {
			try {
				cloudinary.uploader().destroy(oldPublicId, ObjectUtils.emptyMap());
				logger.info("Imagen anterior eliminada de Cloudinary con public_id: {}", oldPublicId);
			} catch (IOException e) {
				logger.warn("Error al eliminar imagen anterior de Cloudinary: {}", e.getMessage());
				// No lanzar excepción para no interrumpir la actualización
			}
		}

		return mapToResponse(gallery);
	}

	@Transactional
	public void deleteGalleryImage(Long id) {
		logger.info("Eliminando imagen de la galería con ID: {}", id);
		Gallery gallery = galleryRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Imagen de la galería no encontrada con ID: " + id));
		try {
			String publicId = extractPublicId(gallery.getImageUrl());
			cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
			logger.info("Imagen eliminada de Cloudinary con public_id: {}", publicId);
		} catch (IOException e) {
			logger.error("Error al eliminar imagen de Cloudinary: {}", e.getMessage());
			throw new CloudinaryUploadException("Error al eliminar la imagen de Cloudinary", e);
		}
		galleryRepository.deleteById(id);
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			logger.warn("Archivo de imagen vacío o nulo");
			throw new IllegalArgumentException("El archivo de imagen es obligatorio");
		}
		if (file.getSize() > MAX_FILE_SIZE) {
			logger.warn("Archivo de imagen excede el tamaño máximo: {} bytes", file.getSize());
			throw new IllegalArgumentException("El archivo excede el tamaño máximo de 5MB");
		}
		if (!ALLOWED_TYPES.contains(file.getContentType())) {
			logger.warn("Tipo de archivo no permitido: {}", file.getContentType());
			throw new IllegalArgumentException("Solo se permiten imágenes JPEG o PNG");
		}
	}

	private String extractPublicId(String url) {
		String[] parts = url.split("/");
		String fileName = parts[parts.length - 1];
		String imageId = fileName.substring(0, fileName.lastIndexOf("."));
		String publicId = String.join("/", parts[parts.length - 3], parts[parts.length - 2], imageId);
		return publicId;
	}

	private void updateEntityFromRequest(Gallery gallery, GalleryImageUpdateRequest request) {
		if (request.getDescription() != null) {
			gallery.setDescription(request.getDescription());
		}
	}

	private GalleryResponse mapToResponse(Gallery gallery) {
		GalleryResponse response = new GalleryResponse();
		response.setId(gallery.getId());
		response.setImageUrl(gallery.getImageUrl());
		response.setDescription(gallery.getDescription());
		response.setUploadedAt(gallery.getUploadedAt());
		return response;
	}
}