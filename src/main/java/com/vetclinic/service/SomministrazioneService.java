package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
public class SomministrazioneService {


    private final SomministrazioneRepository somministrazioneRepository;
    private final MagazzinoService magazzinoService;
    private final AnimaleRepository animaleRepository;
    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;
    private final NotificheService notificheService;
    private final VeterinarioRepository veterinarianRepository;
    private final CronologiaRepository cronologiaRepository;

    public SomministrazioneService(SomministrazioneRepository somministrazioneRepository,
                                   VeterinarioRepository veterinarianRepository,
                                   MagazzinoService magazzinoService,
                                   AnimaleRepository animaleRepository,
                                   UtenteRepository utenteRepository,
                                   CronologiaRepository cronologiaRepository,
                                   AuthenticationService authenticationService, NotificheService notificheService) {
        this.magazzinoService = magazzinoService;
        this.somministrazioneRepository = somministrazioneRepository;
        this.animaleRepository = animaleRepository;
        this.utenteRepository = utenteRepository;
        this.authenticationService = authenticationService;
        this.notificheService = notificheService;
        this.veterinarianRepository = veterinarianRepository;
        this.cronologiaRepository = cronologiaRepository;
    }

    @Transactional
    public Somministrazione registraSomministrazione(Somministrazione somministrazione) {

        Utente utente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (utente == null) {
            throw new IllegalArgumentException("Cliente non trovato");
        }

        if (!utente.getRole().equals("veterinario") || utente.getRole().equals("assistente") || !utente.getRole().equals("capo-reparto")) {
            throw new IllegalArgumentException("Non hai il permesso di assegnare i turni.");
        }

        boolean scorteAggiornate = magazzinoService.updateMedicineStock(somministrazione.getMedicine().getId(), somministrazione.getMedicine().getQuantity());

        if (!scorteAggiornate) {
            notificaAnomalie(somministrazione.getMedicine().getName(), "Quantità insufficiente");
            throw new RuntimeException("Quantità insufficiente per il farmaco con Nome: " + somministrazione.getMedicine().getName());
        }

        somministrazione.setDate(LocalDateTime.now());
        return somministrazioneRepository.save(somministrazione);
    }

    @Transactional
    public void notificaAnomalie(String referenzaId, String tipoAnomalia) {
        System.out.println("Notifica Anomalia - Referenza: " + referenzaId + ", Tipo: " + tipoAnomalia);
        notificheService.sendNotificationAnomalia(referenzaId, tipoAnomalia);
    }

    @Transactional
    public List<Somministrazione> getSomministrazioniByPaziente(Long pazienteId) {
        Animale animale = animaleRepository.findById(pazienteId)
                .orElseThrow(() -> new RuntimeException("Paziente non trovato"));
        return somministrazioneRepository.findByAnimal(animale);
    }


    @Transactional
    public void addDocumentToAnimal(Long animaleId, Long veterinarianId, Long assistantId, String tipo, String contenuto, Object ignored) {
        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new IllegalArgumentException("Animale non trovato"));

        Veterinario veterinario = (Veterinario) utenteRepository.findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinario non trovato"));

        Assistente assistente = (Assistente) utenteRepository.findById(assistantId)
                .orElseThrow(() -> new IllegalArgumentException("Assistente non trovato"));

        String nuovaNota = "[" + tipo.toUpperCase() + "] " + contenuto + " - " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        String notePrecedenti = animale.getVeterinaryNotes() != null ? animale.getVeterinaryNotes() + "\n" : "";
        animale.setVeterinaryNotes(notePrecedenti + nuovaNota);

        animaleRepository.save(animale);
    }

    @Transactional
    public void veterinarianAddDocumentToAnimal(
            Long animaleId,
            Long capoRepartoId,
            Long veterinarioId,
            String tipoDocumento,
            String descrizione,
            byte[] allegato) {

        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new IllegalArgumentException("Animale non trovato"));

        Utente capoReparto = utenteRepository.findById(capoRepartoId)
                .orElseThrow(() -> new IllegalArgumentException("Capo reparto non trovato"));

        Veterinario veterinario = veterinarianRepository.findById(veterinarioId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinario non trovato"));

        CronologiaAnimale cronologia = new CronologiaAnimale();
        cronologia.setAnimaleId(animale.getId());
        cronologia.setVeterinarian(veterinario);
        cronologia.setEventDate(new Date());
        cronologia.setEventType(tipoDocumento);
        cronologia.setDescription(descrizione);

        cronologia.setNoteVet(animale.getVeterinaryNotes());
        cronologia.setSymptoms(animale.getSymptoms());

        cronologiaRepository.save(cronologia);

        notificheService.sendNotificationToSpecificUser(
                capoReparto.getId(),
                "Nuova somministrazione registrata su " + animale.getName() + ": " + descrizione
        );
    }


}
