package com.vetclinic.controller;


import com.vetclinic.models.Fornitore;
import com.vetclinic.service.FornitoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fornitori")
@CrossOrigin(origins = "http://localhost:4200")
public class FornitoreController {

    private final FornitoreService fornitoreService;

    @Autowired
    public FornitoreController(FornitoreService fornitoreService) {
        this.fornitoreService = fornitoreService;
    }


    @GetMapping("/list")
    public ResponseEntity<List<Fornitore>> getAllFornitori() {
        List<Fornitore> fornitori = fornitoreService.getAllFornitori();
        return ResponseEntity.ok(fornitori);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fornitore> getFornitoreById(@PathVariable Long id) {
        Optional<Fornitore> fornitore = fornitoreService.getFornitoreById(id);
        return fornitore.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().body(null));
    }


    @PostMapping("add-fornitore")
    public ResponseEntity<Map<String, String>> addFornitore(@RequestBody Fornitore fornitore) {
        Fornitore createdFornitore = fornitoreService.addFornitore(fornitore);
        return ResponseEntity.ok(Map.of("message", "Fornitore creato con successo!"));
    }


    @PutMapping("/update-fornitore/{id}")
    public ResponseEntity<Map<String, String>> updateFornitore(@PathVariable Long id, @RequestBody Fornitore fornitoreDetails) {
        try {
            fornitoreService.updateFornitore(id, fornitoreDetails);
            return ResponseEntity.ok(Map.of("message", "Fornitore aggiornato con successo!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @DeleteMapping("/delete-fornitore/{id}")
    public ResponseEntity<Map<String, String>> deleteFornitore(@PathVariable Long id) {
        try {
            fornitoreService.deleteFornitore(id);
            return ResponseEntity.ok(Map.of("message", "Fornitore eliminato con successo!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

