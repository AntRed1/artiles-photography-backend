
package com.artiles_photography_backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.artiles_photography_backend.models.AboutUs;
import com.artiles_photography_backend.repository.AboutUsRepository;

/**
 *
 * @author arojas
 */
@Service
public class AboutUsService {

    private final AboutUsRepository aboutUsRepository;

    @Autowired
    public AboutUsService(AboutUsRepository aboutUsRepository) {
        this.aboutUsRepository = aboutUsRepository;
    }

    public List<AboutUs> getAllAboutUs() {
        return (List<AboutUs>) aboutUsRepository.findAll();
    }

    public AboutUs getAboutUsById(Long id) {
        return aboutUsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AboutUs not found with id: " + id));
    }

    public AboutUs saveAboutUs(AboutUs aboutUs) {
        return aboutUsRepository.save(aboutUs);
    }
}
