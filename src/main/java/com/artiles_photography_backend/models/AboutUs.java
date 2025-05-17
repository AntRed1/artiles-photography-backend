package com.artiles_photography_backend.models;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author arojas
 *         * Entidad que representa la información "Sobre Nosotros".
 *
 */

@Entity
@Table(name = "about_us")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AboutUs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "about_us_specialties", joinColumns = @JoinColumn(name = "about_us_id"))
    @Column(name = "specialty")
    private List<String> specialties;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "about_us_specialty_icons", joinColumns = @JoinColumn(name = "about_us_id"))
    @Column(name = "specialty_icon")
    private List<String> specialtyIcons;
}
