package com.vetclinic.controller;

import com.vetclinic.service.EmergenzaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/emergenze")
public class EmergenzaController {

    private final EmergenzaService emergenzaService;

    public EmergenzaController(EmergenzaService emergenzaService) {
        this.emergenzaService = emergenzaService;
    }

    @DeleteMapping("/solve-emergency/{emergenzaId}")
    public ResponseEntity<Map<String, String>> solveEmergency(@PathVariable Long emergenzaId) {
        String result = emergenzaService.solveEmergency(emergenzaId);
        return ResponseEntity.ok(Map.of("message", result));
    }
}
