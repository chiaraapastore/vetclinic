package com.vetclinic.service;

import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service
public class VaccinoService {

    private final VaccinoRepository vaccinoRepository;
    private final AnimaleRepository animaleRepository;
    private final UtenteRepository utenteRepository;

    public VaccinoService(VaccinoRepository vaccinoRepository, AnimaleRepository animaleRepository, UtenteRepository utenteRepository) {
        this.vaccinoRepository = vaccinoRepository;
        this.animaleRepository = animaleRepository;
        this.utenteRepository = utenteRepository;
    }

    @Transactional
    public Vaccino addVaccine(Long animaleId, Long veterinarioId, String nome, String tipo, Date dataSomministrazione) {
        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new RuntimeException("Animale non trovato"));

        Veterinario veterinario = (Veterinario) utenteRepository.findById(veterinarioId)
                .orElseThrow(() -> new RuntimeException("Veterinario non trovato"));

        Vaccino vaccino = new Vaccino();
        vaccino.setName(nome);
        vaccino.setType(tipo);
        vaccino.setAdministrationDate(dataSomministrazione);
        vaccino.setAnimale(animale);
        vaccino.setVeterinario(veterinario);

        return vaccinoRepository.save(vaccino);
    }

    @Transactional
    public List<Vaccino> getVaccineByAnimal(Long animaleId) {
        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new RuntimeException("Animale non trovato"));
        return vaccinoRepository.findByAnimale(animale);
    }

    @Transactional
    public List<Vaccino> getVaccineByVeterinarian(Long veterinarioId) {
        Veterinario veterinario = (Veterinario) utenteRepository.findById(veterinarioId)
                .orElseThrow(() -> new RuntimeException("Veterinario non trovato"));
        return vaccinoRepository.findByVeterinario(veterinario);
    }
}
