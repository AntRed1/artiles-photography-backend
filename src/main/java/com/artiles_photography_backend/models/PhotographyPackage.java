package com.artiles_photography_backend.models;

import java.math.BigDecimal;
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
 *         Entidad que representa un paquete fotográfico ofrecido por Artiles
 *         Photography.
 *
 */
@Entity
@Table(name = "photography_packages")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhotographyPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal price; // CAMBIADO DE Double A BigDecimal

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Boolean showPrice;

    @Column(nullable = false)
    private String imageUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "package_features", joinColumns = @JoinColumn(name = "package_id"))
    @Column(name = "feature")
    private List<String> features;

    // MÉTODOS AGREGADOS PARA ARREGLAR ERRORES DE COMPILACIÓN
    public String getName() {
        return this.title; // Usando title como nombre
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public String getDescription() {
        return this.description;
    }
}