package com.artiles_photography_backend.config;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.artiles_photography_backend.models.AboutUs;
import com.artiles_photography_backend.models.CarouselImage;
import com.artiles_photography_backend.models.Configuration;
import com.artiles_photography_backend.models.ContactInfo;
import com.artiles_photography_backend.models.Gallery;
import com.artiles_photography_backend.models.Legal;
import com.artiles_photography_backend.models.PhotographyPackage;
import com.artiles_photography_backend.models.PhotographyService;
import com.artiles_photography_backend.models.Role;
import com.artiles_photography_backend.models.Testimonial;
import com.artiles_photography_backend.models.User;
import com.artiles_photography_backend.repository.AboutUsRepository;
import com.artiles_photography_backend.repository.CarouselImageRepository;
import com.artiles_photography_backend.repository.ConfigurationRepository;
import com.artiles_photography_backend.repository.ContactInfoRepository;
import com.artiles_photography_backend.repository.GalleryRepository;
import com.artiles_photography_backend.repository.LegalRepository;
import com.artiles_photography_backend.repository.PhotographyPackageRepository;
import com.artiles_photography_backend.repository.PhotographyServiceRepository;
import com.artiles_photography_backend.repository.RoleRepository;
import com.artiles_photography_backend.repository.TestimonialRepository;
import com.artiles_photography_backend.repository.UserRepository;

/**
 * @author arojas
 *         Inicializa datos de prueba en la base de datos si las tablas están
 *         vacías.
 */
@Component
public class DataInitializer implements CommandLineRunner {

        private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

        private final GalleryRepository galleryRepository;
        private final PhotographyPackageRepository photographyPackageRepository;
        private final TestimonialRepository testimonialRepository;
        private final ContactInfoRepository contactInfoRepository;
        private final AboutUsRepository aboutUsRepository;
        private final PhotographyServiceRepository photographyServiceRepository;
        private final LegalRepository legalRepository;
        private final CarouselImageRepository carouselImageRepository;
        private final ConfigurationRepository configurationRepository;
        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;

        public DataInitializer(
                        GalleryRepository galleryRepository,
                        PhotographyPackageRepository photographyPackageRepository,
                        TestimonialRepository testimonialRepository,
                        ContactInfoRepository contactInfoRepository,
                        AboutUsRepository aboutUsRepository,
                        PhotographyServiceRepository photographyServiceRepository,
                        LegalRepository legalRepository,
                        CarouselImageRepository carouselImageRepository,
                        ConfigurationRepository configurationRepository,
                        UserRepository userRepository,
                        RoleRepository roleRepository,
                        PasswordEncoder passwordEncoder) {
                this.galleryRepository = galleryRepository;
                this.photographyPackageRepository = photographyPackageRepository;
                this.testimonialRepository = testimonialRepository;
                this.contactInfoRepository = contactInfoRepository;
                this.aboutUsRepository = aboutUsRepository;
                this.photographyServiceRepository = photographyServiceRepository;
                this.legalRepository = legalRepository;
                this.carouselImageRepository = carouselImageRepository;
                this.configurationRepository = configurationRepository;
                this.userRepository = userRepository;
                this.roleRepository = roleRepository;
                this.passwordEncoder = passwordEncoder;
        }

        @Override
        public void run(String... args) {
                try {
                        initializeRoles();
                        initializeAdminUser();
                        initializeGallery();
                        initializeCarouselImages();
                        initializePhotographyPackages();
                        initializeTestimonials();
                        initializeContactInfo();
                        initializeAboutUs();
                        initializePhotographyServices();
                        initializeLegalDocuments();
                        initializeConfiguration();
                } catch (Exception e) {
                        logger.error("Error durante la inicialización de datos: {}", e.getMessage());
                }
        }

        @Transactional
        private void initializeRoles() {
                if (roleRepository.count() == 0) {
                        logger.info("Inicializando roles...");
                        roleRepository.saveAll(Arrays.asList(
                                        new Role(null, "USER"),
                                        new Role(null, "ADMIN"),
                                        new Role(null, "EDITOR"),
                                        new Role(null, "VISUALIZADOR")));
                }
        }

        @Transactional
        private void initializeAdminUser() {
                if (userRepository.findByEmail("admin@artilesphoto.com").isEmpty()) {
                        logger.info("Inicializando usuario administrador...");
                        Role userRole = roleRepository.findByName("USER")
                                        .orElseThrow(() -> new IllegalStateException("Rol USER no encontrado"));
                        Role adminRole = roleRepository.findByName("ADMIN")
                                        .orElseThrow(() -> new IllegalStateException("Rol ADMIN no encontrado"));

                        User admin = new User();
                        admin.setName("Admin User");
                        admin.setEmail("admin@artilesphoto.com");
                        admin.setPassword(passwordEncoder.encode("admin123"));
                        Set<Role> roles = new HashSet<>();
                        roles.add(userRole);
                        roles.add(adminRole);
                        admin.setRoles(roles);

                        userRepository.save(admin);
                }
        }

        @Transactional
        private void initializeGallery() {
                if (galleryRepository.count() == 0) {
                        logger.info("Inicializando galería...");
                        LocalDateTime now = LocalDateTime.now();
                        galleryRepository.saveAll(Arrays.asList(
                                        new Gallery(null, "/images/gallery1.jpg", "Boda al atardecer",
                                                        now.minusDays(5)),
                                        new Gallery(null, "/images/gallery2.jpg", "Quinceañera en jardín",
                                                        now.minusDays(3)),
                                        new Gallery(null, "/images/gallery3.jpg", "Sesión familiar en playa",
                                                        now.minusDays(1))));
                }
        }

        @Transactional
        private void initializeCarouselImages() {
                if (carouselImageRepository.count() == 0) {
                        logger.info("Inicializando imágenes del carrusel...");
                        carouselImageRepository.saveAll(Arrays.asList(
                                        new CarouselImage(null, "/images/carousel1.jpg", "Momentos Inolvidables",
                                                        "carousel: Momentos Inolvidables", 1),
                                        new CarouselImage(null, "/images/carousel2.jpg", "Capturando Emociones",
                                                        "carousel: Capturando Emociones", 2),
                                        new CarouselImage(null, "/images/carousel3.jpg", "Tu Historia en Imágenes",
                                                        "carousel: Tu Historia en Imágenes", 3)));
                }
        }

        @Transactional
        private void initializePhotographyPackages() {
                if (photographyPackageRepository.count() == 0) {
                        logger.info("Inicializando paquetes fotográficos...");
                        photographyPackageRepository.saveAll(Arrays.asList(
                                        new PhotographyPackage(null, "Paquete Quinceañeras",
                                                        "Sesión completa para quinceañeras", 500.0, true,
                                                        "/images/package1.jpg", Arrays.asList(
                                                                        "Sesión de 4 horas", "50 fotos editadas",
                                                                        "3 cambios de vestuario", "Entrega digital",
                                                                        "Álbum digital")),
                                        new PhotographyPackage(null, "Paquete Bodas", "Cobertura total de bodas",
                                                        1500.0, true, "/images/package2.jpg", Arrays.asList(
                                                                        "Sesión de 8 horas", "200 fotos editadas",
                                                                        "Sesión pre-boda", "Entrega digital",
                                                                        "Video highlights", "USB personalizado")),
                                        new PhotographyPackage(null, "Paquete Graduación", "Sesión para graduaciones",
                                                        300.0, true, "/images/package3.jpg", Arrays.asList(
                                                                        "Sesión de 2 horas", "30 fotos editadas",
                                                                        "2 cambios de vestuario", "Entrega digital",
                                                                        "Álbum digital")),
                                        new PhotographyPackage(null, "Paquete Familiar", "Sesión para familias", 400.0,
                                                        true, "/images/package4.jpg", Arrays.asList(
                                                                        "Sesión de 3 horas", "40 fotos editadas",
                                                                        "Múltiples locaciones", "Entrega digital",
                                                                        "Álbum digital")),
                                        new PhotographyPackage(null, "Paquete Bebés", "Sesión para bebés", 250.0, false,
                                                        "/images/package5.jpg", Arrays.asList(
                                                                        "Sesión de 2 horas", "25 fotos editadas",
                                                                        "Props especiales", "Entrega digital",
                                                                        "Álbum digital")),
                                        new PhotographyPackage(null, "Paquete Eventos", "Cobertura de eventos", 800.0,
                                                        true, "/images/package6.jpg", Arrays.asList(
                                                                        "Sesión de 6 horas", "150 fotos editadas",
                                                                        "Fotografía grupal", "Entrega digital",
                                                                        "Galería en línea"))));
                }
        }

        @Transactional
        private void initializeTestimonials() {
                if (testimonialRepository.count() == 0) {
                        logger.info("Inicializando testimonios...");
                        LocalDateTime now = LocalDateTime.now();
                        testimonialRepository.saveAll(Arrays.asList(
                                        new Testimonial(null, "Ana Pérez", 5,
                                                        "Las fotos de mi quinceañera quedaron espectaculares. ¡Totalmente recomendado!",
                                                        now.minusDays(20), "Mobile Chrome", "192.168.1.1",
                                                        "Santo Domingo, Dominican Republic", true),
                                        new Testimonial(null, "Juan y María", 5,
                                                        "Contratamos sus servicios para nuestra boda y quedamos encantados.",
                                                        now.minusDays(15), "Desktop Firefox", "192.168.1.2",
                                                        "Santo Domingo, Dominican Republic", true),
                                        new Testimonial(null, "Laura Gómez", 4,
                                                        "Excelente servicio para las fotos de graduación de mi hijo.",
                                                        now.minusDays(10), "Tablet Safari", "192.168.1.3",
                                                        "Santo Domingo, Dominican Republic", false)));
                }
        }

        @Transactional
        private void initializeContactInfo() {
                if (contactInfoRepository.count() == 0) {
                        logger.info("Inicializando información de contacto...");
                        contactInfoRepository.save(new ContactInfo(
                                        null,
                                        "(829) 645-1718",
                                        "info@artilesphotography.com",
                                        "Casita de princesa, Quinceañera, Plaza Privada, Av 27 de Febrero esquina privada, Santo Domingo",
                                        "18296451718",
                                        "https://www.facebook.com/people/Casitas-de-princesas-RD/61574858705513/",
                                        "https://www.instagram.com/artilesfotograf",
                                        "https://twitter.com/artilesphoto",
                                        "https://www.tiktok.com/@artilesfotograf",
                                        "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d1892.3236295809074!2d-69.96382959187031!3d18.45431823194794!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x8eaf8b69b1b0ca95%3A0xf15d46b937405300!2sCasita%20de%20princesa%2C%20Quincea%C3%B1era!5e0!3m2!1ses!2sdo!4v1746545688095!5m2!1ses!2sdo"));
                }
        }

        @Transactional
        private void initializeAboutUs() {
                if (aboutUsRepository.count() == 0) {
                        logger.info("Inicializando información Sobre Nosotros...");
                        aboutUsRepository.saveAll(Arrays.asList(
                                        new AboutUs(
                                                        null,
                                                        "Sobre Nosotros",
                                                        "Artiles Photography Studio se dedica a capturar los momentos más importantes de tu vida con creatividad y profesionalismo.",
                                                        Arrays.asList("Quinceañeras", "Bodas", "Graduaciones",
                                                                        "Familias", "Bebés", "Eventos"),
                                                        Arrays.asList("fa-camera", "fa-heart", "fa-graduation-cap",
                                                                        "fa-users", "fa-baby", "fa-calendar")),
                                        new AboutUs(
                                                        null,
                                                        "Misión",
                                                        "Nuestra misión es ofrecer un servicio fotográfico único que capture los momentos más preciados de nuestros clientes con un enfoque en la calidad y la satisfacción.",
                                                        Arrays.asList("Bodas", "Eventos", "Bautizos"),
                                                        Arrays.asList("fa-clipboard", "fa-handshake", "fa-bell"))));
                }
        }

        @Transactional
        private void initializePhotographyServices() {
                if (photographyServiceRepository.count() == 0) {
                        logger.info("Inicializando servicios fotográficos...");
                        List<PhotographyService> services = Arrays.asList(
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
                                        new PhotographyService(null, "Sombrillas Creativas", "fa-umbrella"));

                        for (PhotographyService service : services) {
                                if (photographyServiceRepository.findByTitle(service.getTitle()).isEmpty()) {
                                        photographyServiceRepository.save(service);
                                }
                        }
                }
        }

        @Transactional
        private void initializeLegalDocuments() {
                if (legalRepository.count() == 0) {
                        logger.info("Inicializando documentos legales...");
                        legalRepository.saveAll(Arrays.asList(
                                        new Legal(null, "PRIVACY",
                                                        "En Artiles Photography Studio, respetamos tu privacidad. Recopilamos información personal como nombre, correo electrónico, y datos de contacto solo con tu consentimiento. También podemos recopilar información técnica como el dispositivo, dirección IP, y ubicación geográfica para mejorar nuestros servicios y analizar el uso de nuestro sitio web. No compartimos tus datos con terceros sin tu permiso, salvo lo requerido por ley. Consulta nuestra política completa en nuestro sitio web."),
                                        new Legal(null, "TERMS",
                                                        "Al usar nuestros servicios, aceptas nuestros términos y condiciones. Nos reservamos el derecho de modificar estos términos en cualquier momento.")));
                }
        }

        @Transactional
        private void initializeConfiguration() {
                if (configurationRepository.count() == 0) {
                        logger.info("Inicializando configuración...");
                        configurationRepository.save(new Configuration(
                                        null,
                                        "/images/logo.png",
                                        "/images/hero-background.jpg"));
                }
        }
}