package com.vetclinic.controller;

import com.vetclinic.models.Trattamento;
import com.vetclinic.service.TrattamentoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/treatments")
public class TrattamentoController {

    private final TrattamentoService treatmentService;

    public TrattamentoController(TrattamentoService treatmentService) {
        this.treatmentService = treatmentService;
    }

    @GetMapping("/animal/{animalId}")
    public List<Trattamento> getTreatmentsByAnimal(@PathVariable Long animalId) {
        return treatmentService.getTreatmentsByAnimal(animalId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Trattamento addTreatment(@RequestBody Trattamento treatment) {
        return treatmentService.addTreatment(treatment);
    }
}
