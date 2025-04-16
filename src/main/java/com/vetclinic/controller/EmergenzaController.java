package com.vetclinic.controller;

import com.vetclinic.service.EmergenzaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
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
