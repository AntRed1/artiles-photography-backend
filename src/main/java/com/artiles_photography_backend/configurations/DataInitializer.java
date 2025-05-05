package com.artiles_photography_backend.configurations;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.artiles_photography_backend.models.AboutUs;
import com.artiles_photography_backend.models.Configuration;
import com.artiles_photography_backend.models.ContactInfo;
import com.artiles_photography_backend.models.Gallery;
import com.artiles_photography_backend.models.Legal;
import com.artiles_photography_backend.models.PhotographyPackage;
import com.artiles_photography_backend.models.PhotographyService;
import com.artiles_photography_backend.models.Testimonial;
import com.artiles_photography_backend.services.AboutUsService;
import com.artiles_photography_backend.services.ConfigurationService;
import com.artiles_photography_backend.services.ContactInfoService;
import com.artiles_photography_backend.services.GalleryService;
import com.artiles_photography_backend.services.LegalService;
import com.artiles_photography_backend.services.PhotographyPackageService;
import com.artiles_photography_backend.services.PhotographyServiceService;
import com.artiles_photography_backend.services.TestimonialService;

/**
 *
 * @author arojas
 */
@Component
public class DataInitializer implements CommandLineRunner {

	private final AboutUsService aboutUsService;
	private final ContactInfoService contactInfoService;
	private final GalleryService galleryService;
	private final PhotographyPackageService photographyPackageService;
	private final PhotographyServiceService photographyServiceService;
	private final TestimonialService testimonialService;
	private final LegalService legalService;
	private final ConfigurationService configurationService;

	@Autowired
	public DataInitializer(
			AboutUsService aboutUsService,
			ContactInfoService contactInfoService,
			GalleryService galleryService,
			PhotographyPackageService photographyPackageService,
			PhotographyServiceService photographyServiceService,
			TestimonialService testimonialService,
			LegalService legalService,
			ConfigurationService configurationService) {
		this.aboutUsService = aboutUsService;
		this.contactInfoService = contactInfoService;
		this.galleryService = galleryService;
		this.photographyPackageService = photographyPackageService;
		this.photographyServiceService = photographyServiceService;
		this.testimonialService = testimonialService;
		this.legalService = legalService;
		this.configurationService = configurationService;
	}

	@Override
	public void run(String... args) throws Exception {
		// Initialize AboutUs
		AboutUs aboutUs = new AboutUs();
		aboutUs.setTitle("Sobre Nosotros");
		aboutUs.setContent(
				"Artiles Photography Studio ha estado capturando momentos inolvidables desde 2013. Nos especializamos en fotografía de bodas, eventos familiares y sesiones personalizadas, ofreciendo un servicio de calidad que combina creatividad y profesionalismo.");
		aboutUs.setSpecialties(Arrays.asList(
				"Fotografía de Bodas",
				"Sesiones Familiares",
				"Eventos Corporativos",
				"Retratos Personalizados",
				"Fotografía de Moda",
				"Edición Profesional"));
		aboutUs.setSpecialtyIcons(Arrays.asList(
				"fa-solid fa-heart",
				"fa-solid fa-users",
				"fa-solid fa-briefcase",
				"fa-solid fa-camera",
				"fa-solid fa-star",
				"fa-solid fa-pen"));
		aboutUsService.saveAboutUs(aboutUs);

		// Initialize ContactInfo
		ContactInfo contactInfo = new ContactInfo();
		contactInfo.setPhone("(809) 555-7890");
		contactInfo.setEmail("info@artilesphotography.com");
		contactInfo
				.setAddress("Av. Winston Churchill #123, Plaza Central, Local 45, Santo Domingo, República Dominicana");
		contactInfo.setWhatsapp("18095557890");
		contactInfo.setFacebook("https://facebook.com/artilesphotography");
		contactInfo.setInstagram("https://instagram.com/artilesphotography");
		contactInfo.setTwitter("https://twitter.com/artilesphoto");
		contactInfo.setTiktok("https://tiktok.com/@artilesphotography");
		contactInfoService.saveContactInfo(contactInfo);

		// Initialize Gallery (Carousel and Gallery Images)
		String[] carouselImages = {
				"/images/carousel1.jpg",
				"/images/carousel2.jpg",
				"/images/carousel3.jpg"
		};
		String[] galleryImages = {
				"/images/gallery1.jpg",
				"/images/gallery2.jpg",
				"/images/gallery3.jpg",
				"/images/gallery4.jpg",
				"/images/gallery5.jpg",
				"/images/gallery6.jpg"
		};

		for (int i = 0; i < carouselImages.length; i++) {
			Gallery carouselImage = new Gallery();
			carouselImage.setImageUrl(carouselImages[i]);
			carouselImage.setDescription("carousel: Imagen " + (i + 1));
			carouselImage.setUploadedAt(LocalDateTime.now());
			// TODO: Actualizar cuando GalleryService esté corregido
			// galleryService.saveGallery(carouselImage);
		}

		for (int i = 0; i < galleryImages.length; i++) {
			Gallery galleryImage = new Gallery();
			galleryImage.setImageUrl(galleryImages[i]);
			galleryImage.setDescription("gallery: Imagen " + (i + 1));
			galleryImage.setUploadedAt(LocalDateTime.now());
			// TODO: Actualizar cuando GalleryService esté corregido
			// galleryService.saveGallery(galleryImage);
		}

		// Initialize Photography Packages
		PhotographyPackage[] packages = {
				new PhotographyPackage(
						"Paquete Básico",
						"Sesión de 1 hora con 10 fotos editadas",
						5000.00,
						true,
						"/images/package1.jpg"),
				new PhotographyPackage(
						"Paquete Estándar",
						"Sesión de 2 horas con 20 fotos editadas y álbum",
						10000.00,
						true,
						"/images/package2.jpg"),
				new PhotographyPackage(
						"Paquete Premium",
						"Sesión de 4 horas con 50 fotos editadas, álbum y video",
						20000.00,
						true,
						"/images/package3.jpg")
		};

		for (PhotographyPackage pkg : packages) {
			photographyPackageService.savePhotographyPackage(pkg);
		}

		// Initialize Photography Services
		PhotographyService[] services = {
				new PhotographyService("Impresión de Fotos", "fa-solid fa-print"),
				new PhotographyService("Álbumes Personalizados", "fa-solid fa-book"),
				new PhotographyService("Video de Eventos", "fa-solid fa-video"),
				new PhotographyService("Sesiones en Exterior", "fa-solid fa-tree"),
				new PhotographyService("Fotografía Aérea", "fa-solid fa-drone"),
				new PhotographyService("Edición Avanzada", "fa-solid fa-pen")
		};

		for (PhotographyService service : services) {
			photographyServiceService.savePhotographyService(service);
		}

		// Initialize Testimonials
		Testimonial[] testimonials = {
				new Testimonial(),
				new Testimonial(),
				new Testimonial()
		};
		// TODO: Actualizar cuando TestimonialService esté corregido
		/*
		 * for (Testimonial testimonial : testimonials) {
		 * testimonialService.saveTestimonial(testimonial);
		 * }
		 */

		// Initialize Legal Documents
		Legal privacy = new Legal();
		privacy.setType("PRIVACY");
		privacy.setContent(
				"<h2>Política de Privacidad</h2><p>En Artiles Photography Studio, protegemos su información personal...</p>");
		// TODO: Actualizar cuando LegalService esté corregido
		// legalService.saveLegal(privacy);

		Legal terms = new Legal();
		terms.setType("TERMS");
		terms.setContent("<h2>Términos y Condiciones</h2><p>Al contratar nuestros servicios, usted acepta...</p>");
		// TODO: Actualizar cuando LegalService esté corregido
		// legalService.saveLegal(terms);

		// Initialize Configuration
		Configuration config = new Configuration();
		config.setLogoUrl("/images/logo.png");
		config.setHeroBackgroundImage("/images/hero.jpg");
		configurationService.saveConfiguration(config);
	}
}