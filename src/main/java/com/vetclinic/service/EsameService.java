package com.vetclinic.service;

import com.vetclinic.models.Esame;
import com.vetclinic.repository.EsameRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EsameService {

    private final EsameRepository examRepository;

    public EsameService(EsameRepository examRepository) {
        this.examRepository = examRepository;
    }

    public List<Esame> getExamsByAnimal(Long animalId) {
        return examRepository.findByAnimalId(animalId);
    }

    public Esame addExam(Esame exam) {
        return examRepository.save(exam);
    }
}
