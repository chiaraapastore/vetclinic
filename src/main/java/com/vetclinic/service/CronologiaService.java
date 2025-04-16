package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

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

        Utente veterinario = utenteRepository.findByUsername(authenticationService.getUsername());
        if (veterinario == null) {
            throw new IllegalArgumentException("Veterinario non trovato");
        }

        Utente assistente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (assistente == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }

        CronologiaAnimale cronologiaPaziente = new CronologiaAnimale();
        cronologiaPaziente.setAnimal(animale);
        cronologiaPaziente.setAssistant((Assistente) assistente);
        cronologiaPaziente.setVeterinarian((VeterinarioDTO) veterinario);
        cronologiaPaziente.setEventDate(new Date());
        cronologiaPaziente.setEventType(eventType);
        cronologiaPaziente.setDescription(description);

        cronologiaRepository.save(cronologiaPaziente);
    }

    @Transactional
    public CronologiaAnimale getAnimalHistory(Long clienteId, Long animaleId) {
        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new RuntimeException("Animale non trovato"));

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente non trovato"));

        if (!animale.getCliente().getId().equals(clienteId)) {
            throw new RuntimeException("Questo animale non appartiene al cliente.");
        }

        List<Operazione> operations = new ArrayList<>(operazioneRepository.findByAnimal(animale));
        List<Esame> exams = new ArrayList<>(esameRepository.findByAnimal(animale));
        List<Trattamento> treatments = new ArrayList<>(trattamentoRepository.findByAnimal(animale));
        List<Vaccino> vaccinations = new ArrayList<>(vaccinoRepository.findByAnimale(animale));


        CronologiaAnimale cronologia = new CronologiaAnimale();
        cronologia.setAnimal(animale);
        cronologia.setCliente(cliente);


        cronologia.setOperazione(operations);
        cronologia.setSymptoms(animale.getSymptoms());
        cronologia.setEsame(exams);
        cronologia.setTrattamento(treatments);
        cronologia.setNoteVet(animale.getVeterinaryNotes());
        cronologia.setFollowUp(animale.getNextVisit());
        cronologia.setMedicalHistory(animale.getHistoricalDiseases().toString());
        cronologia.setVaccini(vaccinations);

        return cronologia;
    }
}
