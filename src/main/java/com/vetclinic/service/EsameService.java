package com.vetclinic.service;

import com.vetclinic.models.Esame;
import com.vetclinic.repository.EsameRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EsameService {

    private final EsameRepository examRepository;


    public EsameService(EsameRepository examRepository) {
        this.examRepository = examRepository;
    }

    @Transactional
    public List<Esame> getExamsByAnimal(Long animalId) {
        return examRepository.findByAnimaleId(animalId);
    }

    @Transactional
    public Esame addExam(Esame exam) {
        return examRepository.save(exam);
    }
}
