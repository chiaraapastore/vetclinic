package com.vetclinic.controller;

import com.vetclinic.models.Esame;
import com.vetclinic.service.EsameService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class EsameController {

    private final EsameService examService;

    public EsameController(EsameService examService) {
        this.examService = examService;
    }

    @GetMapping("/animal/{animalId}")
    public List<Esame> getExamsByAnimal(@PathVariable Long animalId) {
        return examService.getExamsByAnimal(animalId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Esame addExam(@RequestBody Esame exam) {
        return examService.addExam(exam);
    }
}
