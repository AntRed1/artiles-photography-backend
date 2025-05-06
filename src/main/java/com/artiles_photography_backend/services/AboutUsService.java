
package com.artiles_photography_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.AboutUs;
import com.artiles_photography_backend.repository.AboutUsRepository;

/**
 *
 * @author arojas
 *         * Servicio para manejar operaciones relacionadas con AboutUs.
 *
 */
@Service
public class AboutUsService {

    private final AboutUsRepository aboutUsRepository;

    @Autowired
    public AboutUsService(AboutUsRepository aboutUsRepository) {
        this.aboutUsRepository = aboutUsRepository;
    }

    public AboutUs getAboutUs() {
        return aboutUsRepository.findAll().stream().findFirst().orElse(null);
    }

    public AboutUs getAboutUsByTitle(String title) {
        return aboutUsRepository.findByTitle(title);
    }

    public AboutUs saveAboutUs(AboutUs aboutUs) {
        return aboutUsRepository.save(aboutUs);
    }
}
