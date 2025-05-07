package com.artiles_photography_backend.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.artiles_photography_backend.dtos.CarouselImageRequest;
import com.artiles_photography_backend.dtos.CarouselImageResponse;
import com.artiles_photography_backend.dtos.CarouselImageUploadRequest;
import com.artiles_photography_backend.exceptions.CloudinaryUploadException;
import com.artiles_photography_backend.models.CarouselImage;
import com.artiles_photography_backend.repository.CarouselImageRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

/**
 * @author arojas
 *         Servicio para manejar operaciones relacionadas con imágenes del
 *         carrusel, incluyendo subida y eliminación en Cloudinary.
 */
@Service
public class CarouselImageService {

    private static final Logger logger = LoggerFactory.getLogger(CarouselImageService.class);
    private static final String CLOUDINARY_FOLDER = "photoquince/carrusel";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png");

    private final CarouselImageRepository carouselImageRepository;
    private final Cloudinary cloudinary;

    public CarouselImageService(CarouselImageRepository carouselImageRepository, Cloudinary cloudinary) {
        this.carouselImageRepository = carouselImageRepository;
        this.cloudinary = cloudinary;
    }

    public List<CarouselImageResponse> getAllCarouselImages() {
        logger.debug("Obteniendo todas las imágenes del carrusel ordenadas por displayOrder");
        return carouselImageRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CarouselImageResponse getCarouselImageById(Long id) {
        logger.debug("Obteniendo imagen del carrusel con ID: {}", id);
        CarouselImage image = carouselImageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Imagen del carrusel no encontrada con ID: " + id));
        return mapToResponse(image);
    }

    @Transactional
    public CarouselImageResponse createCarouselImage(CarouselImageUploadRequest request) {
        logger.info("Subiendo nueva imagen del carrusel con título: {}", request.getTitle());
        validateFile(request.getFile());

        try {
            Map uploadResult = cloudinary.uploader().upload(request.getFile().getBytes(),
                    ObjectUtils.asMap("folder", CLOUDINARY_FOLDER));
            String url = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            CarouselImage image = new CarouselImage();
            image.setUrl(url);
            image.setTitle(request.getTitle());
            image.setDescription(request.getDescription());
            image.setDisplayOrder(request.getDisplayOrder());
            image = carouselImageRepository.save(image);

            logger.info("Imagen subida exitosamente a Cloudinary con public_id: {}", publicId);
            return mapToResponse(image);
        } catch (IOException e) {
            logger.error("Error al subir imagen a Cloudinary: {}", e.getMessage());
            throw new CloudinaryUploadException("Error al subir la imagen a Cloudinary", e);
        }
    }

    @Transactional
    public CarouselImageResponse updateCarouselImage(Long id, CarouselImageRequest request) {
        logger.info("Actualizando imagen del carrusel con ID: {}", id);
        CarouselImage image = carouselImageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Imagen del carrusel no encontrada con ID: " + id));
        updateEntityFromRequest(image, request);
        image = carouselImageRepository.save(image);
        return mapToResponse(image);
    }

    @Transactional
    public void deleteCarouselImage(Long id) {
        logger.info("Eliminando imagen del carrusel con ID: {}", id);
        CarouselImage image = carouselImageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Imagen del carrusel no encontrada con ID: " + id));

        try {
            String publicId = extractPublicId(image.getUrl());
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            logger.info("Imagen eliminada de Cloudinary con public_id: {}", publicId);
        } catch (IOException e) {
            logger.error("Error al eliminar imagen de Cloudinary: {}", e.getMessage());
            throw new CloudinaryUploadException("Error al eliminar la imagen de Cloudinary", e);
        }

        carouselImageRepository.deleteById(id);
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
        // Ejemplo:
        // https://res.cloudinary.com/<cloud_name>/image/upload/v1234567890/photoquince/carrusel/<image_id>.jpg
        String[] parts = url.split("/");
        String fileName = parts[parts.length - 1]; // <image_id>.jpg
        String imageId = fileName.substring(0, fileName.lastIndexOf(".")); // <image_id>
        String publicId = String.join("/", parts[parts.length - 3], parts[parts.length - 2], imageId); // photoquince/carrusel/<image_id>
        return publicId;
    }

    private void updateEntityFromRequest(CarouselImage image, CarouselImageRequest request) {
        image.setUrl(request.getUrl());
        image.setTitle(request.getTitle());
        image.setDescription(request.getDescription());
        image.setDisplayOrder(request.getDisplayOrder());
    }

    private CarouselImageResponse mapToResponse(CarouselImage image) {
        CarouselImageResponse response = new CarouselImageResponse();
        response.setId(image.getId());
        response.setUrl(image.getUrl());
        response.setTitle(image.getTitle());
        response.setDescription(image.getDescription());
        response.setDisplayOrder(image.getDisplayOrder());
        return response;
    }
}