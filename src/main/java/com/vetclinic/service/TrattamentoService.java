package com.vetclinic.service;

import com.vetclinic.models.Trattamento;
import com.vetclinic.repository.TrattamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrattamentoService {

    private final TrattamentoRepository treatmentRepository;

    public TrattamentoService(TrattamentoRepository treatmentRepository) {
        this.treatmentRepository = treatmentRepository;
    }

    public List<Trattamento> getTreatmentsByAnimal(Long animalId) {
        return treatmentRepository.findByAnimalId(animalId);
    }

    public Trattamento addTreatment(Trattamento treatment) {
        return treatmentRepository.save(treatment);
    }
}
