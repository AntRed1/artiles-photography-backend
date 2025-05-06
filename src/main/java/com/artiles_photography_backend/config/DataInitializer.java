package com.artiles_photography_backend.config;

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
 * @author arojas
 *         Inicializa datos de prueba en la base de datos si las tablas están
 *         vacías.
 */
@Component
public class DataInitializer implements CommandLineRunner {

        private final GalleryRepository galleryRepository;
        private final PhotographyPackageRepository photographyPackageRepository;
        private final TestimonialRepository testimonialRepository;
        private final ContactInfoRepository contactInfoRepository;
        private final AboutUsRepository aboutUsRepository;
        private final PhotographyServiceRepository photographyServiceRepository;
        private final LegalRepository legalRepository;
        private final CarouselImageRepository carouselImageRepository;
        private final ConfigurationRepository configurationRepository;

        @Autowired
        public DataInitializer(
                        GalleryRepository galleryRepository,
                        PhotographyPackageRepository photographyPackageRepository,
                        TestimonialRepository testimonialRepository,
                        ContactInfoRepository contactInfoRepository,
                        AboutUsRepository aboutUsRepository,
                        PhotographyServiceRepository photographyServiceRepository,
                        LegalRepository legalRepository,
                        CarouselImageRepository carouselImageRepository,
                        ConfigurationRepository configurationRepository) {
                this.galleryRepository = galleryRepository;
                this.photographyPackageRepository = photographyPackageRepository;
                this.testimonialRepository = testimonialRepository;
                this.contactInfoRepository = contactInfoRepository;
                this.aboutUsRepository = aboutUsRepository;
                this.photographyServiceRepository = photographyServiceRepository;
                this.legalRepository = legalRepository;
                this.carouselImageRepository = carouselImageRepository;
                this.configurationRepository = configurationRepository;
        }

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
                                                        LocalDateTime.of(2025, 4, 15, 0, 0), "Mobile Chrome",
                                                        "192.168.1.1", "Santo Domingo, Dominican Republic", true),
                                        new Testimonial(null, "Juan y María", 5,
                                                        "Contratamos sus servicios para nuestra boda y quedamos encantados.",
                                                        LocalDateTime.of(2025, 3, 22, 0, 0), "Desktop Firefox",
                                                        "192.168.1.2", "Santo Domingo, Dominican Republic", true),
                                        new Testimonial(null, "Laura Gómez", 4,
                                                        "Excelente servicio para las fotos de graduación de mi hijo.",
                                                        LocalDateTime.of(2025, 2, 5, 0, 0), "Tablet Safari",
                                                        "192.168.1.3", "Santo Domingo, Dominican Republic", true)));
                }

                // Contact Info
                if (contactInfoRepository.count() == 0) {
                        contactInfoRepository.save(new ContactInfo(
                                        null,
                                        "(829) 645-1718",
                                        "info@artilesphotography.com",
                                        "Casita de princesa, Quinceañera, Plaza Privada, Av 27 de Febrero esquina privada, Santo Domingo",
                                        "+1 (829) 645-1718",
                                        "https://www.facebook.com/people/Casitas-de-princesas-RD/61574858705513/",
                                        "https://www.instagram.com/artilesfotograf",
                                        "https://twitter.com/artilesphoto",
                                        "https://www.tiktok.com/@artilesfotograf",
                                        "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d1892.3236295809074!2d-69.96382959187031!3d18.45431823194794!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x8eaf8b69b1b0ca95%3A0xf15d46b937405300!2sCasita%20de%20princesa%2C%20Quincea%C3%B1era!5e0!3m2!1ses!2sdo!4v1746545688095!5m2!1ses!2sdo"));
                }

                // About Us
                if (aboutUsRepository.count() == 0) {
                        aboutUsRepository.saveAll(List.of(
                                        new AboutUs(
                                                        null,
                                                        "Sobre Nosotros",
                                                        "Artiles Photography Studio se dedica a capturar los momentos más importantes de tu vida con creatividad y profesionalismo.",
                                                        List.of("Quinceañeras", "Bodas", "Graduaciones", "Familias",
                                                                        "Bebés", "Eventos"),
                                                        List.of("fa-camera", "fa-heart", "fa-graduation-cap",
                                                                        "fa-users", "fa-baby", "fa-calendar")),

                                        // Nuevo registro que puedes agregar
                                        new AboutUs(
                                                        null,
                                                        "Misión",
                                                        "Nuestra misión es ofrecer un servicio fotográfico único que capture los momentos más preciados de nuestros clientes con un enfoque en la calidad y la satisfacción.",
                                                        List.of("Bodas", "Eventos", "Bautizos"),
                                                        List.of("fa-clipboard", "fa-handshake", "fa-bell"))));
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
                                        new Legal(null, "PRIVACY",
                                                        "En Artiles Photography Studio, respetamos tu privacidad. Recopilamos información personal como nombre, correo electrónico, y datos de contacto solo con tu consentimiento. También podemos recopilar información técnica como el dispositivo, dirección IP, y ubicación geográfica para mejorar nuestros servicios y analizar el uso de nuestro sitio web. No compartimos tus datos con terceros sin tu permiso, salvo lo requerido por ley. Consulta nuestra política completa en nuestro sitio web."),
                                        new Legal(null, "TERMS",
                                                        "Al usar nuestros servicios, aceptas nuestros términos y condiciones. Nos reservamos el derecho de modificar estos términos en cualquier momento.")));
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