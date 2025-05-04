package com.vetclinic.controller;


import com.vetclinic.models.Somministrazione;
import com.vetclinic.service.SomministrazioneService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/somministrazioni")
@CrossOrigin(origins = "http://localhost:4200")
public class SomministrazioneController {


    private final SomministrazioneService somministrazioneService;

    public SomministrazioneController(SomministrazioneService somministrazioneService) {
        this.somministrazioneService = somministrazioneService;
    }

    @PostMapping("/registra")
    public ResponseEntity<Somministrazione> registraSomministrazione(@Valid @RequestBody Somministrazione somministrazione) {
        try {
            Somministrazione nuovaSomministrazione = somministrazioneService.registraSomministrazione(somministrazione);
            return ResponseEntity.ok(nuovaSomministrazione);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @GetMapping("/paziente/{pazienteId}")
    public ResponseEntity<List<Somministrazione>> getSomministrazioniByPaziente(@PathVariable Long pazienteId) {
        List<Somministrazione> somministrazioni = somministrazioneService.getSomministrazioniByPaziente(pazienteId);
        return ResponseEntity.ok(somministrazioni);
    }

}