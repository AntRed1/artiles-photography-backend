package com.artiles_photography_backend.config;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.artiles_photography_backend.models.AboutUs;
import com.artiles_photography_backend.models.CarouselImage;
import com.artiles_photography_backend.models.Configuration;
import com.artiles_photography_backend.models.ContactInfo;
import com.artiles_photography_backend.models.EmailTemplate;
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
import com.artiles_photography_backend.repository.EmailTemplateRepository;
import com.artiles_photography_backend.repository.GalleryRepository;
import com.artiles_photography_backend.repository.LegalRepository;
import com.artiles_photography_backend.repository.PhotographyPackageRepository;
import com.artiles_photography_backend.repository.PhotographyServiceRepository;
import com.artiles_photography_backend.repository.RoleRepository;
import com.artiles_photography_backend.repository.TestimonialRepository;
import com.artiles_photography_backend.repository.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

/**
 * Initializes test data in the database if tables are empty, including email
 * templates.
 *
 * @author arojas
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private static final String CLOUDINARY_FOLDER = "photoquince/logo";

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
    private final EmailTemplateRepository emailTemplateRepository;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;

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
            EmailTemplateRepository emailTemplateRepository,
            PasswordEncoder passwordEncoder,
            Cloudinary cloudinary) {
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
        this.emailTemplateRepository = emailTemplateRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinary = cloudinary;
    }

    @Override
    public void run(String... args) {
        logger.info("Starting data initialization...");
        try {
            initializeRolesAndAdminUser();
            initializeGallery();
            initializeCarouselImages();
            initializePhotographyPackages();
            initializeTestimonials();
            initializeContactInfo();
            initializeAboutUs();
            initializePhotographyServices();
            initializeLegalDocuments();
            initializeConfiguration();
            initializeEmailTemplates();
            logger.info("Data initialization completed successfully.");
        } catch (Exception e) {
            logger.error("Error during data initialization: {}", e.getMessage(), e);
            throw new RuntimeException("Data initialization failed", e);
        }
    }

    @Transactional
    private void initializeRolesAndAdminUser() {
        if (roleRepository.count() == 0) {
            logger.info("Initializing roles...");
            roleRepository.saveAll(Arrays.asList(
                    new Role(null, "USER"),
                    new Role(null, "ADMIN"),
                    new Role(null, "EDITOR"),
                    new Role(null, "VISUALIZADOR")));
            logger.info("Roles initialized. Total: {}", roleRepository.count());
        }

        if (userRepository.findByEmail("admin@artilesphoto.com").isEmpty()) {
            logger.info("Initializing admin user...");
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new IllegalStateException("USER role not found"));
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@artilesphoto.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEnabled(true);
            admin.setRoles(new HashSet<>(Arrays.asList(userRole, adminRole)));

            userRepository.save(admin);
            logger.info("Admin user initialized. Total users: {}", userRepository.count());
        }
    }

    @Transactional
    private void initializeGallery() {
        if (galleryRepository.count() == 0) {
            logger.info("Initializing gallery...");
            LocalDateTime now = LocalDateTime.now();
            List<Gallery> galleries = Arrays.asList(
                    createGallery(
                            "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/sample1.jpg",
                            "photoquince/galeria/sample1",
                            "Boda al atardecer",
                            "GALLERY",
                            now.minusDays(5)),
                    createGallery(
                            "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/sample2.jpg",
                            "photoquince/galeria/sample2",
                            "Quinceañera en jardín",
                            "GALLERY",
                            now.minusDays(3)),
                    createGallery(
                            "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/sample3.jpg",
                            "photoquince/galeria/sample3",
                            "Sesión familiar en playa",
                            "GALLERY",
                            now.minusDays(1)));
            galleryRepository.saveAll(galleries);
            logger.info("Gallery initialized. Total images: {}", galleryRepository.count());
        }
    }

    private Gallery createGallery(String imageUrl, String publicId, String description, String type,
            LocalDateTime uploadedAt) {
        Gallery gallery = new Gallery();
        gallery.setImageUrl(imageUrl);
        gallery.setPublicId(publicId);
        gallery.setDescription(description);
        gallery.setType(type);
        gallery.setUploadedAt(uploadedAt);
        return gallery;
    }

    @Transactional
    private void initializeCarouselImages() {
        if (carouselImageRepository.count() == 0) {
            logger.info("Initializing carousel images...");
            carouselImageRepository.saveAll(Arrays.asList(
                    new CarouselImage(null, "/images/carousel1.jpg", "Momentos Inolvidables",
                            "Momentos Inolvidables"),
                    new CarouselImage(null, "/images/carousel2.jpg", "Capturando Emociones",
                            "Capturando Emociones"),
                    new CarouselImage(null, "/images/carousel3.jpg", "Tu Historia en Imágenes",
                            "Tu Historia en Imágenes")));
            logger.info("Carousel initialized. Total images: {}", carouselImageRepository.count());
        }
    }

    @Transactional
    private void initializePhotographyPackages() {
        if (photographyPackageRepository.count() == 0) {
            logger.info("Initializing photography packages...");
            photographyPackageRepository.saveAll(Arrays.asList(
                    new PhotographyPackage(null, "Paquete Quinceañeras",
                            "Sesión completa para quinceañeras", BigDecimal.valueOf(500.0), true, true,
                            "/images/package1.jpg",
                            Arrays.asList("Sesión de 4 horas", "50 fotos editadas",
                                    "3 cambios de vestuario", "Entrega digital",
                                    "Álbum digital")),
                    new PhotographyPackage(null, "Paquete Bodas", "Cobertura total de bodas",
                            BigDecimal.valueOf(1500.0), true, true, "/images/package2.jpg",
                            Arrays.asList("Sesión de 8 horas", "200 fotos editadas",
                                    "Sesión pre-boda", "Entrega digital",
                                    "Video highlights", "USB personalizado")),
                    new PhotographyPackage(null, "Paquete Graduación", "Sesión para graduaciones",
                            BigDecimal.valueOf(300.0), true, true, "/images/package3.jpg",
                            Arrays.asList("Sesión de 2 horas", "30 fotos editadas",
                                    "2 cambios de vestuario", "Entrega digital",
                                    "Álbum digital")),
                    new PhotographyPackage(null, "Paquete Familiar", "Sesión para familias", BigDecimal.valueOf(400.0),
                            true, true, "/images/package4.jpg",
                            Arrays.asList("Sesión de 3 horas", "40 fotos editadas",
                                    "Múltiples locaciones", "Entrega digital",
                                    "Álbum digital")),
                    new PhotographyPackage(null, "Paquete Bebés", "Sesión para bebés", BigDecimal.valueOf(250.0), false,
                            false, "/images/package5.jpg",
                            Arrays.asList("Sesión de 2 horas", "25 fotos editadas",
                                    "Props especiales", "Entrega digital",
                                    "Álbum digital")),
                    new PhotographyPackage(null, "Paquete Eventos", "Cobertura de eventos", BigDecimal.valueOf(800.0),
                            true, true, "/images/package6.jpg",
                            Arrays.asList("Sesión de 6 horas", "150 fotos editadas",
                                    "Fotografía grupal", "Entrega digital",
                                    "Galería en línea"))));
            logger.info("Photography packages initialized. Total: {}",
                    photographyPackageRepository.count());
        }
    }

    @Transactional
    private void initializeTestimonials() {
        if (testimonialRepository.count() == 0) {
            logger.info("Initializing testimonials...");
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
            logger.info("Testimonials initialized. Total: {}", testimonialRepository.count());
        }
    }

    @Transactional
    private void initializeContactInfo() {
        if (contactInfoRepository.count() == 0) {
            logger.info("Initializing contact info...");
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
            logger.info("Contact info initialized. Total: {}", contactInfoRepository.count());
        }
    }

    @Transactional
    private void initializeAboutUs() {
        if (aboutUsRepository.count() == 0) {
            logger.info("Initializing about us...");
            aboutUsRepository.saveAll(Arrays.asList(
                    new AboutUs(null, "Sobre Nosotros",
                            "Artiles Photography Studio se dedica a capturar los momentos más importantes de tu vida con creatividad y profesionalismo.",
                            Arrays.asList("Quinceañeras", "Bodas", "Graduaciones",
                                    "Familias", "Bebés", "Eventos"),
                            Arrays.asList("fa-camera", "fa-heart", "fa-graduation-cap",
                                    "fa-users", "fa-baby", "fa-calendar")),
                    new AboutUs(null, "Misión",
                            "Nuestra misión es ofrecer un servicio fotográfico único que capture los momentos más preciados de nuestros clientes con un enfoque en la calidad y la satisfacción.",
                            Arrays.asList("Bodas", "Eventos", "Bautizos"),
                            Arrays.asList("fa-clipboard", "fa-handshake", "fa-bell"))));
            logger.info("About us initialized. Total: {}", aboutUsRepository.count());
        }
    }

    @Transactional
    private void initializePhotographyServices() {
        if (photographyServiceRepository.count() == 0) {
            logger.info("Initializing photography services...");
            List<PhotographyService> services = Arrays.asList(
                    new PhotographyService(null, "Álbum Digital", "fa-book", "Álbum digital personalizado",
                            BigDecimal.valueOf(50.0)),
                    new PhotographyService(null, "Enmarcados", "fa-image", "Marcos personalizados para fotos",
                            BigDecimal.valueOf(25.0)),
                    new PhotographyService(null, "Llaveros Personalizados", "fa-key",
                            "Llaveros con tus fotos favoritas", BigDecimal.valueOf(10.0)),
                    new PhotographyService(null, "Fotos Impresas", "fa-print", "Impresión de alta calidad",
                            BigDecimal.valueOf(5.0)),
                    new PhotographyService(null, "Vestidos de Embarazadas", "fa-female",
                            "Vestuario para sesiones de maternidad", BigDecimal.valueOf(75.0)),
                    new PhotographyService(null, "Vestidos de Quinceañeras", "fa-crown",
                            "Vestidos elegantes para quinceañeras", BigDecimal.valueOf(150.0)),
                    new PhotographyService(null, "Togas y Birretes", "fa-graduation-cap", "Vestuario para graduaciones",
                            BigDecimal.valueOf(40.0)),
                    new PhotographyService(null, "Coronas", "fa-gem", "Coronas decorativas para eventos",
                            BigDecimal.valueOf(30.0)),
                    new PhotographyService(null, "Números y Letras", "fa-font",
                            "Props personalizados con números y letras", BigDecimal.valueOf(20.0)),
                    new PhotographyService(null, "Pizarras Personalizadas", "fa-chalkboard",
                            "Pizarras con mensajes personalizados", BigDecimal.valueOf(35.0)),
                    new PhotographyService(null, "Arreglos Florales", "fa-leaf", "Decoración floral para sesiones",
                            BigDecimal.valueOf(60.0)),
                    new PhotographyService(null, "Bengalas de Humo", "fa-fire",
                            "Efectos especiales con humo de colores", BigDecimal.valueOf(15.0)),
                    new PhotographyService(null, "Sombrillas Creativas", "fa-umbrella", "Props creativos para sesiones",
                            BigDecimal.valueOf(25.0)));

            for (PhotographyService service : services) {
                if (photographyServiceRepository.findByTitle(service.getTitle()).isEmpty()) {
                    photographyServiceRepository.save(service);
                }
            }
            logger.info("Photography services initialized. Total: {}",
                    photographyServiceRepository.count());
        }
    }

    @Transactional
    private void initializeLegalDocuments() {
        if (legalRepository.count() == 0) {
            logger.info("Initializing legal documents...");
            legalRepository.saveAll(Arrays.asList(
                    new Legal(null, "PRIVACY_POLICY",
                            "En Artiles Photography Studio, respetamos tu privacidad. Recopilamos información personal como nombre, correo electrónico, y datos de contacto solo con tu consentimiento. También podemos recopilar información técnica como el dispositivo, dirección IP, y ubicación geográfica para mejorar nuestros servicios y analizar el uso de nuestro sitio web. No compartimos tus datos con terceros sin tu permiso, salvo lo requerido por ley. Consulta nuestra política completa en nuestro sitio web."),
                    new Legal(null, "TERMS_AND_CONDITIONS",
                            "Al usar nuestros servicios, aceptas nuestros términos y condiciones. Nos reservamos el derecho de modificar estos términos en cualquier momento.")));
            logger.info("Legal documents initialized. Total: {}", legalRepository.count());
        }
    }

    @Transactional
    private void initializeConfiguration() {
        if (configurationRepository.count() == 0) {
            logger.info("Initializing configuration...");
            Configuration config = new Configuration();
            config.setLogoAltText("Artiles Photography Studio Logo");
            config.setHeroBackgroundImage("/images/hero-background.jpg");
            config.setAvailabilityMessage(
                    "Estamos disponibles para sesiones fotográficas según su conveniencia. Contáctenos para agendar.");
            config.setResponseTime("1-2 horas");
            config.setNotificationsEnabled(true);
            config.setReminderCron("0 0 8 * * *"); // Default: 8:00 AM daily

            try {
                ClassPathResource logoResource = new ClassPathResource("static/images/logo.png");
                if (!logoResource.exists()) {
                    logger.warn("Default logo file not found at static/images/logo.png. Using temporary URL.");
                    config.setLogoUrl("https://res.cloudinary.com/demo/image/upload/sample.jpg");
                    config.setLogoPublicId("sample");
                } else {
                    byte[] logoBytes = logoResource.getInputStream().readAllBytes();
                    Map uploadResult = cloudinary.uploader().upload(logoBytes,
                            ObjectUtils.asMap("folder", CLOUDINARY_FOLDER));
                    config.setLogoUrl((String) uploadResult.get("secure_url"));
                    config.setLogoPublicId((String) uploadResult.get("public_id"));
                    logger.info("Logo uploaded to Cloudinary with public_id: {}",
                            config.getLogoPublicId());
                }
            } catch (IOException e) {
                logger.error("Error uploading logo to Cloudinary: {}", e.getMessage());
                config.setLogoUrl("https://res.cloudinary.com/demo/image/upload/sample.jpg");
                config.setLogoPublicId("sample");
            }
            configurationRepository.save(config);
            logger.info("Configuration initialized. Total: {}", configurationRepository.count());
        }
    }

    @Transactional
    private void initializeEmailTemplates() {
        if (emailTemplateRepository.count() == 0) {
            logger.info("Initializing email templates...");
            ContactInfo contactInfo = contactInfoRepository.findAll().stream().findFirst()
                    .orElse(new ContactInfo(null, "", "", "", "", "", "", "", "", ""));

            String facebookUrl = validateUrl(contactInfo.getFacebook()) ? contactInfo.getFacebook() : "";
            String instagramUrl = validateUrl(contactInfo.getInstagram()) ? contactInfo.getInstagram() : "";
            String twitterUrl = validateUrl(contactInfo.getTwitter()) ? contactInfo.getTwitter() : "";
            String tiktokUrl = validateUrl(contactInfo.getTiktok()) ? contactInfo.getTiktok() : "";

            StringBuilder socialMediaLinks = new StringBuilder();
            if (!facebookUrl.isEmpty()) {
                socialMediaLinks.append(String.format(
                        "<a href='%s' style='margin: 0 8px; width: 32px; height: 32px; background-color: #333333; border-radius: 50%%; text-align: center; line-height: 32px; display: inline-block; transition: background-color 0.3s ease;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v13/icons/facebook.svg' alt='Facebook' width='20' height='20' style='vertical-align: middle;'></a>",
                        facebookUrl));
            }
            if (!instagramUrl.isEmpty()) {
                socialMediaLinks.append(String.format(
                        "<a href='%s' style='margin: 0 8px; width: 32px; height: 32px; background-color: #333333; border-radius: 50%%; text-align: center; line-height: 32px; display: inline-block; transition: background-color 0.3s ease;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v13/icons/instagram.svg' alt='Instagram' width='20' height='20' style='vertical-align: middle;'></a>",
                        instagramUrl));
            }
            if (!twitterUrl.isEmpty()) {
                socialMediaLinks.append(String.format(
                        "<a href='%s' style='margin: 0 8px; width: 32px; height: 32px; background-color: #333333; border-radius: 50%%; text-align: center; line-height: 32px; display: inline-block; transition: background-color 0.3s ease;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v13/icons/x.svg' alt='X' width='20' height='20' style='vertical-align: middle;'></a>",
                        twitterUrl));
            }
            if (!tiktokUrl.isEmpty()) {
                socialMediaLinks.append(String.format(
                        "<a href='%s' style='margin: 0 8px; width: 32px; height: 32px; background-color: #333333; border-radius: 50%%; text-align: center; line-height: 32px; display: inline-block; transition: background-color 0.3s ease;'><img src='https://cdn.jsdelivr.net/npm/simple-icons@v13/icons/tiktok.svg' alt='TikTok' width='20' height='20' style='vertical-align: middle;'></a>",
                        tiktokUrl));
            }

            // Plantilla 1: CONTACT_MESSAGE_COMPANY
            String contactMessageCompanyTemplate = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body {
                                font-family: 'Helvetica Neue', 'Arial', 'Verdana', sans-serif;
                                color: #333;
                                line-height: 1.6;
                                margin: 0;
                                padding: 0;
                                background-color: #f4f4f4;
                            }
                            .container {
                                max-width: 600px;
                                margin: 20px auto;
                                padding: 20px;
                                border: 2px solid #e0e0e0;
                                border-radius: 12px;
                                background-color: #f9f9f9;
                                box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                            }
                            .header {
                                background-color: #333333;
                                color: white;
                                padding: 20px;
                                text-align: center;
                                border-radius: 10px 10px 0 0;
                            }
                            .header img {
                                max-width: 160px;
                                height: auto;
                                margin-bottom: 10px;
                            }
                            .content {
                                padding: 25px;
                                background-color: white;
                                border-radius: 0 0 10px 10px;
                            }
                            .greeting {
                                margin-bottom: 20px;
                                font-size: 16px;
                                font-weight: 500;
                            }
                            .field {
                                margin-bottom: 20px;
                            }
                            .field-label {
                                font-weight: bold;
                                color: #444;
                                display: flex;
                                align-items: center;
                            }
                            .field-label::before {
                                content: '📷';
                                margin-right: 8px;
                                font-size: 16px;
                            }
                            .field-value {
                                margin: 5px 0 0 24px;
                                color: #555;
                            }
                            .action-button {
                                display: inline-block;
                                padding: 12px 24px;
                                margin-top: 20px;
                                background-color: #e6b800;
                                color: white;
                                text-decoration: none;
                                border-radius: 8px;
                                font-size: 16px;
                                font-weight: 600;
                                box-shadow: 0 2px 4px rgba(0,0,0,0.2);
                                transition: background-color 0.3s ease, transform 0.2s ease;
                            }
                            .action-button:hover {
                                background-color: #cc9900;
                                transform: translateY(-2px);
                            }
                            .footer {
                                text-align: center;
                                margin-top: 25px;
                                font-size: 12px;
                                color: #777;
                            }
                            .footer a {
                                color: #e6b800;
                                text-decoration: none;
                            }
                            .footer a:hover {
                                text-decoration: underline;
                            }
                            .social-media a {
                                display: inline-block;
                                margin: 0 8px;
                                width: 32px;
                                height: 32px;
                                background-color: #333333;
                                border-radius: 50%;
                                text-align: center;
                                line-height: 32px;
                                transition: background-color 0.3s ease;
                            }
                            .social-media img {
                                width: 20px;
                                height: 20px;
                                vertical-align: middle;
                            }
                            .social-media a:hover {
                                background-color: #e6b800;
                            }
                            @media screen and (max-width: 600px) {
                                .container {
                                    margin: 10px;
                                    padding: 15px;
                                }
                                .header {
                                    padding: 15px;
                                }
                                .header img {
                                    max-width: 120px;
                                }
                                .content {
                                    padding: 15px;
                                }
                                .action-button {
                                    padding: 10px 20px;
                                    font-size: 14px;
                                }
                                .social-media a {
                                    width: 28px;
                                    height: 28px;
                                    line-height: 28px;
                                }
                                .social-media img {
                                    width: 18px;
                                    height: 18px;
                                }
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <img src="{{logoUrl}}" alt="Artiles Photography Studio">
                                <h2>Nuevo Mensaje de Contacto</h2>
                            </div>
                            <div class="content">
                                <p class="greeting">Estimado equipo de Artiles Photography Studio,</p>
                                <p>Se ha recibido un nuevo mensaje de contacto a través del sitio web.</p>
                                <div class="field">
                                    <span class="field-label">Nombre:</span>
                                    <p class="field-value">{{name}}</p>
                                </div>
                                <div class="field">
                                    <span class="field-label">Correo Electrónico:</span>
                                    <p class="field-value">{{email}}</p>
                                </div>
                                <div class="field">
                                    <span class="field-label">Teléfono:</span>
                                    <p class="field-value">{{phone}}</p>
                                </div>
                                <div class="field">
                                    <span class="field-label">Servicio de Interés:</span>
                                    <p class="field-value">{{service}}</p>
                                </div>
                                <div class="field">
                                    <span class="field-label">Mensaje:</span>
                                    <p class="field-value">{{message}}</p>
                                </div>
                                <div class="field">
                                    <span class="field-label">Fecha de Envío:</span>
                                    <p class="field-value">{{createdAt}}</p>
                                </div>
                                <div class="field">
                                    <span class="field-label">IP del Cliente:</span>
                                    <p class="field-value">{{clientIp}}</p>
                                </div>
                                <div class="field">
                                    <span class="field-label">User Agent:</span>
                                    <p class="field-value">{{userAgent}}</p>
                                </div>
                                <a href="mailto:{{email}}" class="action-button">Responder al Cliente</a>
                            </div>
                            <div class="footer">
                                <p>Enviado desde Artiles Photography Studio</p>
                                <p><a href="https://artilesphotography.com">artilesphotography.com</a></p>
                                <div class="social-media">
                                    """ + socialMediaLinks.toString() + """
                                </div>
                            </div>
                        </div>
                    </body>
                    </html>""";

            // Plantilla 2: CONTACT_MESSAGE_CLIENT
            String contactMessageClientTemplate = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body {
                                font-family: 'Helvetica Neue', 'Arial', 'Verdana', sans-serif;
                                color: #333;
                                line-height: 1.6;
                                margin: 0;
                                padding: 0;
                                background-color: #f4f4f4;
                            }
                            .container {
                                max-width: 600px;
                                margin: 20px auto;
                                padding: 20px;
                                border: 2px solid #e0e0e0;
                                border-radius: 12px;
                                background-color: #f9f9f9;
                                box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                            }
                            .header {
                                background-color: #333333;
                                color: white;
                                padding: 20px;
                                text-align: center;
                                border-radius: 10px 10px 0 0;
                            }
                            .header img {
                                max-width: 160px;
                                height: auto;
                                margin-bottom: 10px;
                            }
                            .content {
                                padding: 25px;
                                background-color: white;
                                border-radius: 0 0 10px 10px;
                            }
                            .greeting {
                                margin-bottom: 20px;
                                font-size: 16px;
                                font-weight: 500;
                            }
                            .field {
                                margin-bottom: 20px;
                            }
                            .field-label {
                                font-weight: bold;
                                color: #444;
                                display: flex;
                                align-items: center;
                            }
                            .field-label::before {
                                content: '📷';
                                margin-right: 8px;
                                font-size: 16px;
                            }
                            .field-value {
                                margin: 5px 0 0 24px;
                                color: #555;
                            }
                            .action-button {
                                display: inline-block;
                                padding: 12px 24px;
                                margin-top: 20px;
                                background-color: #e6b800;
                                color: white;
                                text-decoration: none;
                                border-radius: 8px;
                                font-size: 16px;
                                font-weight: 600;
                                box-shadow: 0 2px 4px rgba(0,0,0,0.2);
                                transition: background-color 0.3s ease, transform 0.2s ease;
                            }
                            .action-button:hover {
                                background-color: #cc9900;
                                transform: translateY(-2px);
                            }
                            .footer {
                                text-align: center;
                                margin-top: 25px;
                                font-size: 12px;
                                color: #777;
                            }
                            .footer a {
                                color: #e6b800;
                                text-decoration: none;
                            }
                            .footer a:hover {
                                text-decoration: underline;
                            }
                            .social-media a {
                                display: inline-block;
                                margin: 0 8px;
                                width: 32px;
                                height: 32px;
                                background-color: #333333;
                                border-radius: 50%;
                                text-align: center;
                                line-height: 32px;
                                transition: background-color 0.3s ease;
                            }
                            .social-media img {
                                width: 20px;
                                height: 20px;
                                vertical-align: middle;
                            }
                            .social-media a:hover {
                                background-color: #e6b800;
                            }
                            @media screen and (max-width: 600px) {
                                .container {
                                    margin: 10px;
                                    padding: 15px;
                                }
                                .header {
                                    padding: 15px;
                                }
                                .header img {
                                    max-width: 120px;
                                }
                                .content {
                                    padding: 15px;
                                }
                                .action-button {
                                    padding: 10px 20px;
                                    font-size: 14px;
                                }
                                .social-media a {
                                    width: 28px;
                                    height: 28px;
                                    line-height: 28px;
                                }
                                .social-media img {
                                    width: 18px;
                                    height: 18px;
                                }
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <img src="{{logoUrl}}" alt="Artiles Photography Studio">
                                <h2>Confirmación de Mensaje</h2>
                            </div>
                            <div class="content">
                                <p class="greeting">Estimado/a {{name}},</p>
                                <p>Gracias por contactar con Artiles Photography Studio. Hemos recibido tu mensaje y te contactaremos pronto para discutir los detalles.</p>
                                <div class="field">
                                    <span class="field-label">Servicio de Interés:</span>
                                    <p class="field-value">{{service}}</p>
                                </div>
                                <div class="field">
                                    <span class="field-label">Mensaje:</span>
                                    <p class="field-value">{{message}}</p>
                                </div>
                                <div class="field">
                                    <span class="field-label">Fecha de Envío:</span>
                                    <p class="field-value">{{createdAt}}</p>
                                </div>
                                <a href="https://artilesphotography.com/contact" class="action-button">Contáctenos</a>
                            </div>
                            <div class="footer">
                                <p>Artiles Photography Studio</p>
                                <p><a href="https://artilesphotography.com">artilesphotography.com</a></p>
                                <div class="social-media">
                                    """
                    + socialMediaLinks.toString() + """
                                        </div>
                                    </div>
                                </div>
                            </body>
                            </html>""";

            // Plantilla 3: CUSTOM_EMAIL
            String customEmailTemplate = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body {
                                font-family: 'Helvetica Neue', 'Arial', 'Verdana', sans-serif;
                                color: #333;
                                line-height: 1.6;
                                margin: 0;
                                padding: 0;
                                background-color: #f4f4f4;
                            }
                            .container {
                                max-width: 600px;
                                margin: 20px auto;
                                padding: 20px;
                                border: 2px solid #e0e0e0;
                                border-radius: 12px;
                                background-color: #f9f9f9;
                                box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                            }
                            .header {
                                background-color: #333333;
                                color: white;
                                padding: 20px;
                                text-align: center;
                                border-radius: 10px 10px 0 0;
                            }
                            .header img {
                                max-width: 160px;
                                height: auto;
                                margin-bottom: 10px;
                            }
                            .content {
                                padding: 25px;
                                background-color: white;
                                border-radius: 0 0 10px 10px;
                            }
                            .greeting {
                                margin-bottom: 20px;
                                font-size: 16px;
                                font-weight: 500;
                            }
                            .field {
                                margin-bottom: 20px;
                            }
                            .field-label {
                                font-weight: bold;
                                color: #444;
                                display: flex;
                                align-items: center;
                            }
                            .field-label::before {
                                content: '📷';
                                margin-right: 8px;
                                font-size: 16px;
                            }
                            .field-value {
                                margin: 5px 0 0 24px;
                                color: #555;
                            }
                            .action-button {
                                display: inline-block;
                                padding: 12px 24px;
                                margin-top: 20px;
                                background-color: #e6b800;
                                color: white;
                                text-decoration: none;
                                border-radius: 8px;
                                font-size: 16px;
                                font-weight: 600;
                                box-shadow: 0 2px 4px rgba(0,0,0,0.2);
                                transition: background-color 0.3s ease, transform 0.2s ease;
                            }
                            .action-button:hover {
                                background-color: #cc9900;
                                transform: translateY(-2px);
                            }
                            .footer {
                                text-align: center;
                                margin-top: 25px;
                                font-size: 12px;
                                color: #777;
                            }
                            .footer a {
                                color: #e6b800;
                                text-decoration: none;
                            }
                            .footer a:hover {
                                text-decoration: underline;
                            }
                            .social-media a {
                                display: inline-block;
                                margin: 0 8px;
                                width: 32px;
                                height: 32px;
                                background-color: #333333;
                                border-radius: 50%;
                                text-align: center;
                                line-height: 32px;
                                transition: background-color 0.3s ease;
                            }
                            .social-media img {
                                width: 20px;
                                height: 20px;
                                vertical-align: middle;
                            }
                            .social-media a:hover {
                                background-color: #e6b800;
                            }
                            @media screen and (max-width: 600px) {
                                .container {
                                    margin: 10px;
                                    padding: 15px;
                                }
                                .header {
                                    padding: 15px;
                                }
                                .header img {
                                    max-width: 120px;
                                }
                                .content {
                                    padding: 15px;
                                }
                                .action-button {
                                    padding: 10px 20px;
                                    font-size: 14px;
                                }
                                .social-media a {
                                    width: 28px;
                                    height: 28px;
                                    line-height: 28px;
                                }
                                .social-media img {
                                    width: 18px;
                                    height: 18px;
                                }
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <img src="{{logoUrl}}" alt="Artiles Photography Studio">
                                <h2>{{subject}}</h2>
                            </div>
                            <div class="content">
                                <p class="greeting">Estimado/a Cliente,</p>
                                <p>Gracias por su interés en Artiles Photography Studio. A continuación, los detalles de su mensaje personalizado:</p>
                                <div class="field">
                                    <span class="field-label">Fecha:</span>
                                    <p class="field-value">{{date}}</p>
                                </div>
                                <div class="field">
                                    <span class="field-label">Mensaje:</span>
                                    <p class="field-value">{{body}}</p>
                                </div>
                                <a href="https://artilesphotography.com/contact" class="action-button">Contáctenos</a>
                            </div>
                            <div class="footer">
                                <p>Artiles Photography Studio</p>
                                <p><a href="https://artilesphotography.com">artilesphotography.com</a></p>
                                <div class="social-media">
                                    """
                    + socialMediaLinks.toString() + """
                                        </div>
                                    </div>
                                </div>
                            </body>
                            </html>""";

            // Plantilla 4: APPOINTMENT_REMINDER
            String appointmentReminderTemplate = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body {
                                font-family: 'Helvetica Neue', 'Arial', 'Verdana', sans-serif;
                                color: #333;
                                line-height: 1.6;
                                margin: 0;
                                padding: 0;
                                background-color: #f4f4f4;
                            }
                            .container {
                                max-width: 600px;
                                margin: 20px auto;
                                padding: 20px;
                                border: 2px solid #e0e0e0;
                                border-radius: 12px;
                                background-color: #f9f9f9;
                                box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                            }
                            .header {
                                background-color: #333333;
                                color: white;
                                padding: 20px;
                                text-align: center;
                                border-radius: 10px 10px 0 0;
                            }
                            .header img {
                                max-width: 160px;
                                height: auto;
                                margin-bottom: 10px;
                            }
                            .content {
                                padding: 25px;
                                background-color: white;
                                border-radius: 0 0 10px 10px;
                            }
                            .greeting {
                                margin-bottom: 20px;
                                font-size: 16px;
                                font-weight: 500;
                            }
                            .field {
                                margin-bottom: 20px;
                            }
                            .field-label {
                                font-weight: bold;
                                color: #444;
                                display: flex;
                                align-items: center;
                            }
                            .field-label::before {
                                content: '📷';
                                margin-right: 8px;
                                font-size: 16px;
                            }
                            .field-value {
                                margin: 5px 0 0 24px;
                                color: #555;
                            }
                            .action-button {
                                display: inline-block;
                                padding: 12px 24px;
                                margin-top: 20px;
                                background-color: #e6b800;
                                color: white;
                                text-decoration: none;
                                border-radius: 8px;
                                font-size: 16px;
                                font-weight: 600;
                                box-shadow: 0 2px 4px rgba(0,0,0,0.2);
                                transition: background-color 0.3s ease, transform 0.2s ease;
                            }
                            .action-button:hover {
                                background-color: #cc9900;
                                transform: translateY(-2px);
                            }
                            .footer {
                                text-align: center;
                                margin-top: 25px;
                                font-size: 12px;
                                color: #777;
                            }
                            .footer a {
                                color: #e6b800;
                                text-decoration: none;
                            }
                            .footer a:hover {
                                text-decoration: underline;
                            }
                            .social-media a {
                                display: inline-block;
                                margin: 0 8px;
                                width: 32px;
                                height: 32px;
                                background-color: #333333;
                                border-radius: 50%;
                                text-align: center;
                                line-height: 32px;
                                transition: background-color 0.3s ease;
                            }
                            .social-media img {
                                width: 20px;
                                height: 20px;
                                vertical-align: middle;
                            }
                            .social-media a:hover {
                                background-color: #e6b800;
                            }
                            @media screen and (max-width: 600px) {
                                .container {
                                    margin: 10px;
                                    padding: 15px;
                                }
                                .header {
                                    padding: 15px;
                                }
                                .header img {
                                    max-width: 120px;
                                }
                                .content {
                                    padding: 15px;
                                }
                                .action-button {
                                    padding: 10px 20px;
                                    font-size: 14px;
                                }
                                .social-media a {
                                    width: 28px;
                                    height: 28px;
                                    line-height: 28px;
                                }
                                .social-media img {
                                    width: 18px;
                                    height: 18px;
                                }
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <img src="{{logoUrl}}" alt="Artiles Photography Studio">
                                <h2>Recordatorio de Cita</h2>
                            </div>
                            <div class="content">
                                <p class="greeting">Estimado/a {{clientName}},</p>
                                <p>Este es un recordatorio de su cita con Artiles Photography Studio.</p>
                                <div class="field">
                                    <span class="field-label">Título:</span>
                                    <p class="field-value">{{title}}</p>
                                </div>
                                <div class="field">
                                    <span class="field-label">Fecha y Hora:</span>
                                    <p class="field-value">{{startTime}}</p>
                                </div>
                                <div class="field">
                                    <span class="field-label">Ubicación:</span>
                                    <p class="field-value">{{location}}</p>
                                </div>
                                <div class="field">
                                    <span class="field-label">Descripción:</span>
                                    <p class="field-value">{{description}}</p>
                                </div>
                                <a href="https://artilesphotography.com/contact" class="action-button">Contáctenos</a>
                            </div>
                            <div class="footer">
                                <p>Artiles Photography Studio</p>
                                <p><a href="https://artilesphotography.com">artilesphotography.com</a></p>
                                <div class="social-media">
                                    """ + socialMediaLinks.toString() + """
                                </div>
                            </div>
                        </div>
                    </body>
                    </html>""";

            List<EmailTemplate> templates = Arrays.asList(
                    new EmailTemplate(null, "CONTACT_MESSAGE_COMPANY",
                            "Notificación de nuevo mensaje de contacto para la empresa",
                            "Nuevo Mensaje de Contacto - Artiles Photography Studio",
                            contactMessageCompanyTemplate, LocalDateTime.now(), null),
                    new EmailTemplate(null, "CONTACT_MESSAGE_CLIENT",
                            "Confirmación de mensaje de contacto para el cliente",
                            "Confirmación de Mensaje - Artiles Photography Studio",
                            contactMessageClientTemplate, LocalDateTime.now(), null),
                    new EmailTemplate(null, "CUSTOM_EMAIL",
                            "Plantilla para correos personalizados",
                            "Correo Personalizado",
                            customEmailTemplate, LocalDateTime.now(), null),
                    new EmailTemplate(null, "APPOINTMENT_REMINDER",
                            "Recordatorio de citas para contactos",
                            "Recordatorio de Cita",
                            appointmentReminderTemplate, LocalDateTime.now(), null));

            for (EmailTemplate template : templates) {
                logger.debug("Saving email template: {}, type: {}", template.getTemplateName(),
                        template.getType());
                emailTemplateRepository.save(template);
            }
            logger.info("Email templates initialized. Total: {}", emailTemplateRepository.count());
        }
    }

    private boolean validateUrl(String url) {
        return url != null && !url.trim().isEmpty()
                && (url.startsWith("http://") || url.startsWith("https://"));
    }
}