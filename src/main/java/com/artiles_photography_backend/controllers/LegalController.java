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
 */
@RestController
@RequestMapping("/api/legal")
public class LegalController {
    @Autowired
    private LegalService legalService;

    @GetMapping
    public ResponseEntity<List<Legal>> getLegalDocuments() {
        return ResponseEntity.ok(legalService.getLegalDocuments());
    }

    @PostMapping
    public ResponseEntity<Legal> saveLegalDocument(@RequestBody Legal legal) {
        return ResponseEntity.ok(legalService.saveLegalDocument(legal));
    }
}
