package com.artiles_photography_backend.models;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Data;

/**
 *
 * @author arojas
 */

@Entity
@Table(name = "information")
@Data
public class AboutUs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ElementCollection
    @CollectionTable(name = "about_us_specialties", joinColumns = @JoinColumn(name = "about_us_id"))
    @Column(name = "specialty")
    private List<String> specialties;

    @ElementCollection
    @CollectionTable(name = "about_us_specialty_icons", joinColumns = @JoinColumn(name = "about_us_id"))
    @Column(name = "icon")
    private List<String> specialtyIcons;
}
