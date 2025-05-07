package com.artiles_photography_backend.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.dtos.LegalRequest;
import com.artiles_photography_backend.dtos.LegalResponse;
import com.artiles_photography_backend.services.LegalService;

import jakarta.validation.Valid;

/**
 * @author arojas
 *         Controlador REST para manejar peticiones relacionadas con Legal.
 */
@RestController
@RequestMapping("/api/legal")
public class LegalController {

    private final LegalService legalService;

    public LegalController(LegalService legalService) {
        this.legalService = legalService;
    }

    @GetMapping
    public ResponseEntity<List<LegalResponse>> getAllLegals() {
        return ResponseEntity.ok(legalService.getAllLegals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LegalResponse> getLegalById(@PathVariable Long id) {
        return ResponseEntity.ok(legalService.getLegalById(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<LegalResponse> getLegalByType(@PathVariable String type) {
        return ResponseEntity.ok(legalService.getLegalByType(type));
    }

    @PostMapping("/admin/legal")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LegalResponse> createLegal(@Valid @RequestBody LegalRequest request) {
        return ResponseEntity.status(201).body(legalService.createLegal(request));
    }

    @PutMapping("/admin/legal/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LegalResponse> updateLegal(@PathVariable Long id, @Valid @RequestBody LegalRequest request) {
        return ResponseEntity.ok(legalService.updateLegal(id, request));
    }

    @DeleteMapping("/admin/legal/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLegal(@PathVariable Long id) {
        legalService.deleteLegal(id);
        return ResponseEntity.noContent().build();
    }
}