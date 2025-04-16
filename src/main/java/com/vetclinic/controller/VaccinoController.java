package com.vetclinic.controller;

import com.vetclinic.models.Vaccino;
import com.vetclinic.service.VaccinoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/vaccini")
public class VaccinoController {

    private final VaccinoService vaccinoService;

    public VaccinoController(VaccinoService vaccinoService) {
        this.vaccinoService = vaccinoService;
    }

    @PostMapping("/add")
    public ResponseEntity<Vaccino> addVaccine(
            @RequestParam Long animaleId,
            @RequestParam Long veterinarioId,
            @RequestParam String nome,
            @RequestParam String tipo,
            @RequestParam Date dataSomministrazione) {

        Vaccino vaccino = vaccinoService.addVaccine(animaleId, veterinarioId, nome, tipo, dataSomministrazione);
        return ResponseEntity.ok(vaccino);
    }

    @GetMapping("/animale/{animaleId}")
    public ResponseEntity<List<Vaccino>> getVaccineByAnimal(@PathVariable Long animaleId) {
        List<Vaccino> vaccini = vaccinoService.getVaccineByAnimal(animaleId);
        return ResponseEntity.ok(vaccini);
    }

    @GetMapping("/veterinario/{veterinarioId}")
    public ResponseEntity<List<Vaccino>> getVaccineByVeterinarian(@PathVariable Long veterinarioId) {
        List<Vaccino> vaccini = vaccinoService.getVaccineByVeterinarian(veterinarioId);
        return ResponseEntity.ok(vaccini);
    }
}
