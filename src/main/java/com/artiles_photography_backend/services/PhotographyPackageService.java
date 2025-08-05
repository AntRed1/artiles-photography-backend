package com.artiles_photography_backend.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.artiles_photography_backend.dtos.PhotographyPackageRequest;
import com.artiles_photography_backend.dtos.PhotographyPackageResponse;
import com.artiles_photography_backend.dtos.PhotographyPackageSelectRequest;
import com.artiles_photography_backend.dtos.PhotographyPackageUploadRequest;
import com.artiles_photography_backend.exceptions.CloudinaryUploadException;
import com.artiles_photography_backend.models.PhotographyPackage;
import com.artiles_photography_backend.repository.PhotographyPackageRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

/**
 * @author arojas
 *         Servicio para manejar operaciones relacionadas con paquetes
 *         fotográficos, incluyendo subida y eliminación en Cloudinary.
 */
@Service
public class PhotographyPackageService {

    private static final Logger logger = LoggerFactory.getLogger(PhotographyPackageService.class);
    private static final String CLOUDINARY_FOLDER = "photoquince/paquetes";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png");

    private final PhotographyPackageRepository repository;
    private final Cloudinary cloudinary;

    public PhotographyPackageService(PhotographyPackageRepository repository, Cloudinary cloudinary) {
        this.repository = repository;
        this.cloudinary = cloudinary;
    }

    public List<PhotographyPackageResponse> getAllPhotographyPackages() {
        logger.debug("Obteniendo todos los paquetes fotográficos");
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PhotographyPackageResponse selectPhotographyPackageImage(PhotographyPackageSelectRequest request) {
        logger.info("Seleccionando imagen existente para paquete con ID: {} y URL: {}", request.getId(),
                request.getImageUrl());
        validateUrl(request.getImageUrl());
        PhotographyPackage pkg = repository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado con ID: " + request.getId()));
        pkg.setImageUrl(request.getImageUrl());
        pkg.setTitle(request.getTitle());
        pkg.setDescription(request.getDescription());
        pkg.setPrice(request.getPrice());
        pkg.setIsActive(request.getIsActive());
        pkg.setShowPrice(request.getShowPrice());
        pkg.setFeatures(request.getFeatures());
        pkg = repository.save(pkg);
        return mapToResponse(pkg);
    }

    private void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            logger.warn("URL de imagen inválida o nula");
            throw new IllegalArgumentException("La URL de la imagen es obligatoria");
        }
        if (!url.startsWith("http") || !url.contains("cloudinary")) {
            logger.warn("URL no válida para Cloudinary: {}", url);
            throw new IllegalArgumentException("La URL debe ser una URL válida de Cloudinary");
        }
    }

    public PhotographyPackageResponse getById(Long id) {
        logger.debug("Obteniendo paquete fotográfico con ID: {}", id);
        PhotographyPackage pkg = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado con ID: " + id));
        return mapToResponse(pkg);
    }

    public List<PhotographyPackageResponse> getActivePhotographyPackages() {
        logger.debug("Obteniendo paquetes fotográficos activos");
        return repository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PhotographyPackageResponse createPhotographyPackage(PhotographyPackageUploadRequest request) {
        logger.info("Creando nuevo paquete fotográfico con título: {}", request.getTitle());
        validateFile(request.getFile(), true);

        try {
            Map uploadResult = cloudinary.uploader().upload(request.getFile().getBytes(),
                    ObjectUtils.asMap("folder", CLOUDINARY_FOLDER));
            String url = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            PhotographyPackage pkg = new PhotographyPackage();
            pkg.setImageUrl(url);
            pkg.setTitle(request.getTitle());
            pkg.setDescription(request.getDescription());
            pkg.setPrice(request.getPrice());
            pkg.setIsActive(request.getIsActive());
            pkg.setShowPrice(request.getShowPrice());
            pkg.setFeatures(request.getFeatures());
            pkg = repository.save(pkg);

            logger.info("Paquete creado y subido a Cloudinary con public_id: {}", publicId);
            return mapToResponse(pkg);
        } catch (IOException e) {
            logger.error("Error al subir imagen a Cloudinary: {}", e.getMessage());
            throw new CloudinaryUploadException("Error al subir la imagen a Cloudinary", e);
        }
    }

    @Transactional
    public PhotographyPackageResponse updatePhotographyPackage(Long id, PhotographyPackageRequest request) {
        logger.info("Actualizando paquete fotográfico con ID: {}", id);
        PhotographyPackage pkg = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado con ID: " + id));
        updateEntityFromRequest(pkg, request);
        pkg = repository.save(pkg);
        return mapToResponse(pkg);
    }

    @Transactional
    public PhotographyPackageResponse createPhotographyPackageWithCloudinary(PhotographyPackageSelectRequest request) {
        logger.info("Creando nuevo paquete fotográfico con imagen de Cloudinary, publicId: {}", request.getPublicId());

        if (request.getPublicId() == null || request.getPublicId().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El publicId es obligatorio para crear un paquete con imagen de Cloudinary");
        }

        try {
            // Obtener la URL de la imagen desde Cloudinary usando el publicId
            Map resourceResult = cloudinary.api().resource(request.getPublicId(), ObjectUtils.emptyMap());
            String url = (String) resourceResult.get("secure_url");

            if (url == null || url.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "No se pudo obtener la URL para el publicId: " + request.getPublicId());
            }

            PhotographyPackage pkg = new PhotographyPackage();
            pkg.setImageUrl(url);
            pkg.setTitle(request.getTitle());
            pkg.setDescription(request.getDescription());
            pkg.setPrice(request.getPrice());
            pkg.setIsActive(request.getIsActive());
            pkg.setShowPrice(request.getShowPrice());
            pkg.setFeatures(request.getFeatures());
            pkg = repository.save(pkg);

            logger.info("Paquete creado con imagen de Cloudinary: {} -> {}", request.getPublicId(), url);
            return mapToResponse(pkg);
        } catch (Exception e) {
            logger.error("Error al crear paquete con imagen de Cloudinary: {}", e.getMessage());
            throw new CloudinaryUploadException("Error al obtener la imagen de Cloudinary", e);
        }
    }

    @Transactional
    public PhotographyPackageResponse updatePhotographyPackage(Long id, PhotographyPackageUploadRequest request) {
        logger.info("Actualizando paquete fotográfico con ID: {}", id);
        PhotographyPackage pkg = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado con ID: " + id));

        // Actualizar campos básicos
        pkg.setTitle(request.getTitle());
        pkg.setDescription(request.getDescription());
        pkg.setPrice(request.getPrice());
        pkg.setIsActive(request.getIsActive());
        pkg.setShowPrice(request.getShowPrice());
        pkg.setFeatures(request.getFeatures());

        // Manejar la imagen
        MultipartFile file = request.getFile();
        String publicId = request.getPublicId();

        logger.info("Parámetros de imagen - File presente: {}, PublicId: {}",
                file != null && !file.isEmpty(), publicId);

        // Caso 1: Se proporciona un archivo para subir
        if (file != null && !file.isEmpty()) {
            logger.info("Actualizando con archivo local");
            validateFile(file, false);
            try {
                // Eliminar la imagen anterior en Cloudinary (si existe)
                if (pkg.getImageUrl() != null && pkg.getImageUrl().contains("cloudinary")) {
                    String oldPublicId = extractPublicId(pkg.getImageUrl());
                    cloudinary.uploader().destroy(oldPublicId, ObjectUtils.emptyMap());
                    logger.info("Imagen anterior eliminada de Cloudinary con public_id: {}", oldPublicId);
                }

                // Subir la nueva imagen
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                        ObjectUtils.asMap("folder", CLOUDINARY_FOLDER));
                String url = (String) uploadResult.get("secure_url");
                String newPublicId = (String) uploadResult.get("public_id");
                pkg.setImageUrl(url);
                logger.info("Nueva imagen subida a Cloudinary con public_id: {}", newPublicId);
            } catch (IOException e) {
                logger.error("Error al subir imagen a Cloudinary: {}", e.getMessage());
                throw new CloudinaryUploadException("Error al subir la imagen a Cloudinary", e);
            }
        }
        // Caso 2: Se proporciona un publicId de Cloudinary
        else if (publicId != null && !publicId.trim().isEmpty()) {
            logger.info("Actualizando con imagen de Cloudinary, publicId: {}", publicId);
            try {
                // Eliminar la imagen anterior si es diferente
                if (pkg.getImageUrl() != null && pkg.getImageUrl().contains("cloudinary")) {
                    String oldPublicId = extractPublicId(pkg.getImageUrl());
                    if (!oldPublicId.equals(publicId)) {
                        cloudinary.uploader().destroy(oldPublicId, ObjectUtils.emptyMap());
                        logger.info("Imagen anterior eliminada de Cloudinary con public_id: {}", oldPublicId);
                    }
                }

                // Obtener la URL de la imagen desde Cloudinary usando el publicId
                Map resourceResult = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
                String url = (String) resourceResult.get("secure_url");

                if (url == null || url.trim().isEmpty()) {
                    throw new IllegalArgumentException("No se pudo obtener la URL para el publicId: " + publicId);
                }

                pkg.setImageUrl(url);
                logger.info("Imagen actualizada desde Cloudinary: {} -> {}", publicId, url);
            } catch (Exception e) {
                logger.error("Error al obtener imagen de Cloudinary con publicId {}: {}", publicId, e.getMessage());
                throw new CloudinaryUploadException("Error al obtener la imagen de Cloudinary", e);
            }
        }
        // Caso 3: No se proporciona ni archivo ni publicId (mantener imagen actual)
        else {
            logger.info("No se proporciona nueva imagen, manteniendo la actual");
        }

        pkg = repository.save(pkg);
        logger.info("Paquete fotográfico actualizado exitosamente con ID: {}", id);
        return mapToResponse(pkg);
    }

    @Transactional
    public void deletePhotographyPackage(Long id) {
        logger.info("Eliminando paquete fotográfico con ID: {}", id);
        PhotographyPackage pkg = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado con ID: " + id));

        try {
            String publicId = extractPublicId(pkg.getImageUrl());
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            logger.info("Imagen eliminada de Cloudinary con public_id: {}", publicId);
        } catch (IOException e) {
            logger.error("Error al eliminar imagen de Cloudinary: {}", e.getMessage());
            throw new CloudinaryUploadException("Error al eliminar la imagen de Cloudinary", e);
        }

        repository.deleteById(id);
    }

    private void validateFile(MultipartFile file, boolean required) {
        if (required && (file == null || file.isEmpty())) {
            logger.warn("Archivo de imagen vacío o nulo");
            throw new IllegalArgumentException("El archivo de imagen es obligatorio");
        }
        if (file != null && !file.isEmpty()) {
            if (file.getSize() > MAX_FILE_SIZE) {
                logger.warn("Archivo de imagen excede el tamaño máximo: {} bytes", file.getSize());
                throw new IllegalArgumentException("El archivo excede el tamaño máximo de 5MB");
            }
            if (!ALLOWED_TYPES.contains(file.getContentType())) {
                logger.warn("Tipo de archivo no permitido: {}", file.getContentType());
                throw new IllegalArgumentException("Solo se permiten imágenes JPEG o PNG");
            }
        }
    }

    private String extractPublicId(String url) {
        String[] parts = url.split("/");
        String fileName = parts[parts.length - 1];
        String imageId = fileName.substring(0, fileName.lastIndexOf("."));
        String publicId = String.join("/", parts[parts.length - 3], parts[parts.length - 2], imageId);
        return publicId;
    }

    private void updateEntityFromRequest(PhotographyPackage pkg, PhotographyPackageRequest request) {
        pkg.setImageUrl(request.getImageUrl());
        pkg.setTitle(request.getTitle());
        pkg.setDescription(request.getDescription());
        pkg.setPrice(request.getPrice());
        pkg.setIsActive(request.getIsActive());
        pkg.setShowPrice(request.getShowPrice());
        pkg.setFeatures(request.getFeatures());
    }

    private PhotographyPackageResponse mapToResponse(PhotographyPackage pkg) {
        PhotographyPackageResponse response = new PhotographyPackageResponse();
        response.setId(pkg.getId());
        response.setImageUrl(pkg.getImageUrl());
        response.setTitle(pkg.getTitle());
        response.setDescription(pkg.getDescription());
        response.setPrice(pkg.getPrice());
        response.setIsActive(pkg.getIsActive());
        response.setShowPrice(pkg.getShowPrice());
        response.setFeatures(pkg.getFeatures());
        return response;
    }
}