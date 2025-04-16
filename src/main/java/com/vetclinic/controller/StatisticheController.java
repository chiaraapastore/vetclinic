package com.vetclinic.controller;

import com.vetclinic.service.StatisticheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/statistiche")
public class StatisticheController {

    private final StatisticheService statisticheService;

    public StatisticheController(StatisticheService statisticheService) {
        this.statisticheService = statisticheService;
    }


    @GetMapping("/consumi")
    public ResponseEntity<Map<String, Object>> getConsumiNelTempo() {
        Map<String, Object> consumiNelTempo = statisticheService.getConsumiNelTempo();
        return ResponseEntity.ok(consumiNelTempo);
    }

    @GetMapping("/riordini")
    public ResponseEntity<Map<String, Object>> getRiordiniStockout() {
        Map<String, Object> riordiniStockout = statisticheService.getRiordiniStockout();
        return ResponseEntity.ok(riordiniStockout);
    }

    @GetMapping("/distribuzione-reparti")
    public ResponseEntity<List<Map<String, Object>>> getDistribuzionePerReparto() {
        List<Map<String, Object>> distribuzionePerReparto = statisticheService.getDistribuzionePerReparto();
        return ResponseEntity.ok(distribuzionePerReparto);
    }
}
