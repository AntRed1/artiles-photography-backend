package com.artiles_photography_backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artiles_photography_backend.models.Legal;
import com.artiles_photography_backend.services.LegalService;

/**
 *
 * @author arojas
 *         * Controlador REST para manejar peticiones relacionadas con Legal.
 * 
 */
@RestController
@RequestMapping("/api/legal")
public class LegalController {

    private final LegalService legalService;

    @Autowired
    public LegalController(LegalService legalService) {
        this.legalService = legalService;
    }

    /**
     * Obtiene todos los documentos legales.
     */
    @GetMapping
    public ResponseEntity<List<Legal>> getAllLegals() {
        return ResponseEntity.ok(legalService.getAllLegals());
    }

    /**
     * Guarda un nuevo documento legal.
     */
    @PostMapping
    public ResponseEntity<Legal> saveLegal(@RequestBody Legal legal) {
        return ResponseEntity.ok(legalService.saveLegal(legal));
    }
}
