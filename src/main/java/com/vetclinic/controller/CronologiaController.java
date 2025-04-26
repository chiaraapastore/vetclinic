package com.vetclinic.controller;

import com.vetclinic.models.AddEventRequest;
import com.vetclinic.models.ApiResponse;
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


    @GetMapping("/animal-history/{animaleId}")
    public ResponseEntity<List<CronologiaAnimale>> getAnimalFullHistory(@PathVariable Long animaleId) {
        List<CronologiaAnimale> history = cronologiaService.getFullAnimalHistory(animaleId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/add/{animaleId}")
    public ResponseEntity<ApiResponse> addEventToAnimal(@PathVariable Long animaleId, @RequestBody AddEventRequest request) {
        cronologiaService.addEventToAnimal(animaleId, request.getEventType(), request.getDescription());
        return ResponseEntity.ok(new ApiResponse("Evento aggiunto con successo"));
    }







}
