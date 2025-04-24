package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CronologiaService {

    private final CronologiaRepository cronologiaRepository;
    private final AnimaleRepository animaleRepository;
    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;
    private final VaccinoRepository vaccinoRepository;
    private final OperazioneRepository operazioneRepository;
    private final EsameRepository esameRepository;
    private final TrattamentoRepository trattamentoRepository;
    private final ClienteRepository clienteRepository;

    public CronologiaService(CronologiaRepository cronologiaRepository, AnimaleRepository animaleRepository, UtenteRepository utenteRepository, AuthenticationService authenticationService, VaccinoRepository vaccinoRepository,
                             OperazioneRepository operazioneRepository, EsameRepository esameRepository,
                             TrattamentoRepository trattamentoRepository, ClienteRepository clienteRepository) {
        this.cronologiaRepository = cronologiaRepository;
        this.animaleRepository = animaleRepository;
        this.utenteRepository = utenteRepository;
        this.authenticationService = authenticationService;
        this.vaccinoRepository = vaccinoRepository;
        this.operazioneRepository = operazioneRepository;
        this.esameRepository = esameRepository;
        this.trattamentoRepository = trattamentoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public void addEventToAnimal(Long animaleId, String eventType, String description) {

        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new IllegalArgumentException("Animale non trovato"));

        Utente utente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (utente == null) {
            throw new IllegalArgumentException("Utente non trovato");
        }

        Veterinario veterinario = null;
        Assistente assistente = null;

        if (utente instanceof Veterinario) {
            veterinario = (Veterinario) utente;
        } else if (utente instanceof Assistente) {
            assistente = (Assistente) utente;
        }

        if (veterinario == null && assistente == null) {
            throw new IllegalArgumentException("L'utente non è né un veterinario né un assistente");
        }

        CronologiaAnimale cronologiaPaziente = new CronologiaAnimale();
        cronologiaPaziente.setAnimaleId(animale.getId());
        cronologiaPaziente.setEventDate(new Date());
        cronologiaPaziente.setEventType(eventType);
        cronologiaPaziente.setDescription(description);

        if (veterinario != null) {
            cronologiaPaziente.setVeterinarian(veterinario);
        }
        if (assistente != null) {
            cronologiaPaziente.setAssistente(assistente);
        }

        if (animale.getCliente() != null) {
            cronologiaPaziente.setCliente(animale.getCliente());
        }

        cronologiaRepository.save(cronologiaPaziente);
    }


    @Transactional
    public CronologiaAnimale getAnimalHistory(Long clienteId, Long animaleId) {

        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new RuntimeException("Animale non trovato"));


        if (!animale.getCliente().getId().equals(clienteId)) {
            throw new RuntimeException("Questo animale non appartiene al cliente.");
        }

        List<CronologiaAnimale> history = cronologiaRepository.findByAnimaleId(animaleId);

        List<Operazione> operations = operazioneRepository.findByAnimaleId(animaleId);
        List<Trattamento> treatments = trattamentoRepository.findByAnimalId(animaleId);
        List<Vaccino> vaccinations = vaccinoRepository.findByAnimaleId(animaleId);
        List<Esame> exams = esameRepository.findByAnimaleId(animaleId);

        CronologiaAnimale cronologia = history.isEmpty() ? null : history.get(0);

        if (!operations.isEmpty()) {
            cronologia.setOperazioneId(operations.get(0).getId());
        }

        cronologia.setSymptoms(animale.getSymptoms());
        cronologia.setNoteVet(animale.getVeterinaryNotes());
        cronologia.setFollowUp(animale.getNextVisit());

        if (!treatments.isEmpty()) {
            cronologia.setTrattamentoId(treatments.get(0).getId());
        }

        if (!vaccinations.isEmpty()) {
            cronologia.setVaccinoId(vaccinations.get(0).getId());
        }

        if (!exams.isEmpty()) {
            cronologia.setEsameId(exams.get(0).getId());
        }

        cronologia.setEventDate(new Date());
        cronologia.setEventType("TipoEvento");

        return cronologia;
    }






}
