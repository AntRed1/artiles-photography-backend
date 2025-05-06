package com.artiles_photography_backend.configurations;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.artiles_photography_backend.models.AboutUs;
import com.artiles_photography_backend.models.CarouselImage;
import com.artiles_photography_backend.models.Configuration;
import com.artiles_photography_backend.models.ContactInfo;
import com.artiles_photography_backend.models.Gallery;
import com.artiles_photography_backend.models.Legal;
import com.artiles_photography_backend.models.PhotographyPackage;
import com.artiles_photography_backend.models.PhotographyService;
import com.artiles_photography_backend.models.Testimonial;
import com.artiles_photography_backend.repository.AboutUsRepository;
import com.artiles_photography_backend.repository.CarouselImageRepository;
import com.artiles_photography_backend.repository.ConfigurationRepository;
import com.artiles_photography_backend.repository.ContactInfoRepository;
import com.artiles_photography_backend.repository.GalleryRepository;
import com.artiles_photography_backend.repository.LegalRepository;
import com.artiles_photography_backend.repository.PhotographyPackageRepository;
import com.artiles_photography_backend.repository.PhotographyServiceRepository;
import com.artiles_photography_backend.repository.TestimonialRepository;

/**
 *
 * @author arojas
 */
@Component
public class DataInitializer implements CommandLineRunner {

        @Autowired
        private GalleryRepository galleryRepository;

        @Autowired
        private PhotographyPackageRepository photographyPackageRepository;

        @Autowired
        private TestimonialRepository testimonialRepository;

        @Autowired
        private ContactInfoRepository contactInfoRepository;

        @Autowired
        private AboutUsRepository aboutUsRepository;

        @Autowired
        private PhotographyServiceRepository photographyServiceRepository;

        @Autowired
        private LegalRepository legalRepository;

        @Autowired
        private CarouselImageRepository carouselImageRepository;

        @Autowired
        private ConfigurationRepository configurationRepository;

        @Override
        public void run(String... args) throws Exception {
                // Gallery
                if (galleryRepository.count() == 0) {
                        galleryRepository.saveAll(List.of(
                                        new Gallery(null, "/images/gallery1.jpg", "Boda al atardecer",
                                                        LocalDateTime.of(2025, 5, 1, 0, 0)),
                                        new Gallery(null, "/images/gallery2.jpg", "Quinceañera en jardín",
                                                        LocalDateTime.of(2025, 5, 2, 0, 0)),
                                        new Gallery(null, "/images/gallery3.jpg", "Sesión familiar en playa",
                                                        LocalDateTime.of(2025, 5, 3, 0, 0))));
                }

                // Carousel Images
                if (carouselImageRepository.count() == 0) {
                        carouselImageRepository.saveAll(List.of(
                                        new CarouselImage(null, "/images/carousel1.jpg", "Momentos Inolvidables",
                                                        "carousel: Momentos Inolvidables", 1),
                                        new CarouselImage(null, "/images/carousel2.jpg", "Capturando Emociones",
                                                        "carousel: Capturando Emociones", 2),
                                        new CarouselImage(null, "/images/carousel3.jpg", "Tu Historia en Imágenes",
                                                        "carousel: Tu Historia en Imágenes", 3)));
                }

                // Photography Packages
                if (photographyPackageRepository.count() == 0) {
                        photographyPackageRepository.saveAll(List.of(
                                        new PhotographyPackage(null, "Paquete Quinceañeras",
                                                        "Sesión completa para quinceañeras", 500.0, true,
                                                        "/images/package1.jpg", List.of(
                                                                        "Sesión de 4 horas",
                                                                        "50 fotos editadas",
                                                                        "3 cambios de vestuario",
                                                                        "Entrega digital",
                                                                        "Álbum digital")),
                                        new PhotographyPackage(null, "Paquete Bodas", "Cobertura total de bodas",
                                                        1500.0, true, "/images/package2.jpg", List.of(
                                                                        "Sesión de 8 horas",
                                                                        "200 fotos editadas",
                                                                        "Sesión pre-boda",
                                                                        "Entrega digital",
                                                                        "Video highlights",
                                                                        "USB personalizado")),
                                        new PhotographyPackage(null, "Paquete Graduación", "Sesión para graduaciones",
                                                        300.0, true, "/images/package3.jpg", List.of(
                                                                        "Sesión de 2 horas",
                                                                        "30 fotos editadas",
                                                                        "2 cambios de vestuario",
                                                                        "Entrega digital",
                                                                        "Álbum digital")),
                                        new PhotographyPackage(null, "Paquete Familiar", "Sesión para familias", 400.0,
                                                        true, "/images/package4.jpg", List.of(
                                                                        "Sesión de 3 horas",
                                                                        "40 fotos editadas",
                                                                        "Múltiples locaciones",
                                                                        "Entrega digital",
                                                                        "Álbum digital")),
                                        new PhotographyPackage(null, "Paquete Bebés", "Sesión para bebés", 250.0, true,
                                                        "/images/package5.jpg", List.of(
                                                                        "Sesión de 2 horas",
                                                                        "25 fotos editadas",
                                                                        "Props especiales",
                                                                        "Entrega digital",
                                                                        "Álbum digital")),
                                        new PhotographyPackage(null, "Paquete Eventos", "Cobertura de eventos", 800.0,
                                                        true, "/images/package6.jpg", List.of(
                                                                        "Sesión de 6 horas",
                                                                        "150 fotos editadas",
                                                                        "Fotografía grupal",
                                                                        "Entrega digital",
                                                                        "Galería en línea"))));
                }

                // Testimonials
                if (testimonialRepository.count() == 0) {
                        testimonialRepository.saveAll(List.of(
                                        new Testimonial(null, "Ana Pérez", 5,
                                                        "Las fotos de mi quinceañera quedaron espectaculares. ¡Totalmente recomendado!",
                                                        LocalDateTime.of(2025, 4, 15, 0, 0)),
                                        new Testimonial(null, "Juan y María", 5,
                                                        "Contratamos sus servicios para nuestra boda y quedamos encantados.",
                                                        LocalDateTime.of(2025, 3, 22, 0, 0)),
                                        new Testimonial(null, "Laura Gómez", 4,
                                                        "Excelente servicio para las fotos de graduación de mi hijo.",
                                                        LocalDateTime.of(2025, 2, 5, 0, 0))));
                }

                // Contact Info
                if (contactInfoRepository.count() == 0) {
                        contactInfoRepository.save(new ContactInfo(
                                        null,
                                        "(809) 555-7890",
                                        "info@artilesphotography.com",
                                        "Av. Winston Churchill #123, Plaza Central, Local 45, Santo Domingo, República Dominicana",
                                        "18095557890",
                                        "https://facebook.com/artilesphotography",
                                        "https://instagram.com/artilesphotography",
                                        "https://twitter.com/artilesphoto",
                                        "https://tiktok.com/@artilesphotography",
                                        "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3783.055!2d-69.938093!3d18.475174!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x0%3A0x0!2zMTjCsDI4JzMwLjYiTiA2OcKwNTYnMTYuOCJX!5e0!3m2!1sen!2sus!4v1698765432101"));
                }

                // About Us
                if (aboutUsRepository.count() == 0) {
                        aboutUsRepository.save(new AboutUs(
                                        null,
                                        "Sobre Nosotros",
                                        "Artiles Photography Studio se dedica a capturar los momentos más importantes de tu vida con creatividad y profesionalismo.",
                                        List.of("Quinceañeras", "Bodas", "Graduaciones", "Familias", "Bebés",
                                                        "Eventos"),
                                        List.of("fa-camera", "fa-heart", "fa-graduation-cap", "fa-users", "fa-baby",
                                                        "fa-calendar")));
                }

                // Photography Services
                if (photographyServiceRepository.count() == 0) {
                        photographyServiceRepository.saveAll(List.of(
                                        new PhotographyService(null, "Álbum Digital", "fa-book"),
                                        new PhotographyService(null, "Enmarcados", "fa-image"),
                                        new PhotographyService(null, "Llaveros Personalizados", "fa-key"),
                                        new PhotographyService(null, "Fotos Impresas", "fa-print"),
                                        new PhotographyService(null, "Vestidos de Embarazadas", "fa-female"),
                                        new PhotographyService(null, "Vestidos de Quinceañeras", "fa-crown"),
                                        new PhotographyService(null, "Togas y Birretes", "fa-graduation-cap"),
                                        new PhotographyService(null, "Coronas", "fa-gem"),
                                        new PhotographyService(null, "Números y Letras", "fa-font"),
                                        new PhotographyService(null, "Pizarras Personalizadas", "fa-chalkboard"),
                                        new PhotographyService(null, "Arreglos Florales", "fa-leaf"),
                                        new PhotographyService(null, "Bengalas de Humo", "fa-fire"),
                                        new PhotographyService(null, "Sombrillas Creativas", "fa-umbrella")));
                }

                // Legal Documents
                if (legalRepository.count() == 0) {
                        legalRepository.saveAll(List.of(
                                        new Legal(null, "PRIVACY", "Política de privacidad del estudio..."),
                                        new Legal(null, "TERMS", "Términos y condiciones del servicio...")));
                }

                // Configuration
                if (configurationRepository.count() == 0) {
                        configurationRepository.save(new Configuration(
                                        null,
                                        "/images/logo.png",
                                        "/images/hero-background.jpg"));
                }
        }
}