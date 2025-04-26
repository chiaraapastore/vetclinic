package com.vetclinic.controller;

import com.vetclinic.service.CronologiaService;
import com.vetclinic.models.CronologiaAnimale;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/cronologia")
public class CronologiaController {

    private final CronologiaService cronologiaService;

    public CronologiaController(CronologiaService cronologiaService) {
        this.cronologiaService = cronologiaService;
    }

    @PostMapping("/add/{animaleId}")
    public ResponseEntity<String> addEventToAnimal(@PathVariable Long animaleId, @RequestBody CronologiaAnimale cronologiaAnimale) {
        cronologiaService.addEventToAnimal(animaleId, cronologiaAnimale.getEventType(), cronologiaAnimale.getDescription());
        return ResponseEntity.ok("Evento aggiunto con successo");
    }

    @GetMapping("/animal-history/{animaleId}")
    public ResponseEntity<List<CronologiaAnimale>> getFullAnimalHistory(@PathVariable Long animaleId) {
        List<CronologiaAnimale> history = cronologiaService.getFullAnimalHistory(animaleId);
        return ResponseEntity.ok(history);
    }

}
