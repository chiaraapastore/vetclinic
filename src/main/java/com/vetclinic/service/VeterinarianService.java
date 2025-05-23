package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VeterinarianService {

    private final RepartoRepository repartoRepository;
    private final MedicineRepository medicineRepository;
    private final AnimaleRepository animaleRepository;
    private final AuthenticationService authenticationService;
    private final UtenteRepository utenteRepository;
    private final SomministrazioneRepository somministrazioneRepository;
    private final NotificheService notificheService;
    private final VeterinarioRepository veterinarioRepository;
    private final SomministrazioneService somministrazioneService;


    public VeterinarianService(RepartoRepository repartoRepository,  SomministrazioneService somministrazioneService,VeterinarioRepository veterinarioRepository,MedicineRepository medicineRepository, AnimaleRepository animaleRepository, AuthenticationService authenticationService, UtenteRepository utenteRepository, SomministrazioneRepository  somministrazioneRepository, NotificheService notificheService) {
        this.repartoRepository = repartoRepository;
        this.medicineRepository = medicineRepository;
        this.animaleRepository = animaleRepository;
        this.authenticationService = authenticationService;
        this.utenteRepository = utenteRepository;
        this.somministrazioneRepository = somministrazioneRepository;
        this.notificheService = notificheService;
        this.veterinarioRepository = veterinarioRepository;
        this.somministrazioneService = somministrazioneService;

    }

    @Transactional
    public String executeOperation(Long pazienteId, String tipoOperazione, String descrizioneOperazione) {
        try {
            Utente utente = utenteRepository.findByUsername(authenticationService.getUsername());
            if (utente == null) {
                throw new IllegalArgumentException("Utente non trovato");
            }

            Animale animale = animaleRepository.findById(pazienteId)
                    .orElseThrow(() -> new RuntimeException("Paziente non trovato"));

            Veterinario veterinario = (Veterinario) utente;


            animale.setState("Operato");
            animaleRepository.save(animale);

            return "Operazione eseguita con successo per il paziente " + animale.getName();

        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'esecuzione dell'operazione: " + e.getMessage(), e);
        }
    }



    @Transactional
    public String  administersMedicines(Long pazienteId, Long capoRepartoId, Long medicineId, int quantita) {
        try {

            Utente veterinario = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario non trovato"));

            Utente assistente = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assistente non trovato"));

            Utente capoReparto = utenteRepository.findCapoRepartoById(capoRepartoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capo Reparto non trovato"));

            Animale animale = animaleRepository.findById(pazienteId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animale non trovato"));

            Medicine medicine = medicineRepository.findById(medicineId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicinale non trovato"));

            if (medicine.getAvailableQuantity() < quantita) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantità insufficiente del medicinale");
            }

            medicine.setAvailableQuantity(medicine.getAvailableQuantity() - quantita);
            medicineRepository.save(medicine);

            notificheService.sendNotificationSomministration( veterinario,capoReparto, medicine.getName());

            Somministrazione somministrazione = new Somministrazione();
            somministrazione.setAnimal(animale);
            somministrazione.setMedicine(medicine);
            somministrazione.setDosage(quantita);
            somministrazione.setDate(LocalDateTime.now());
            somministrazione.setVeterinario(veterinario);
            somministrazione.setAssistente(assistente);
            medicine.setDepartment(animale.getReparto());
            somministrazioneRepository.save(somministrazione);

            somministrazioneService.veterinarianAddDocumentToAnimal(
                    pazienteId,
                    capoRepartoId,
                    veterinario.getId(),
                    "SOMMINISTRAZIONE",
                    "Somministrazione farmaco: " + medicine.getName(),
                    null
            );


            return "Farmaco somministrato con successo!";

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la somministrazione: " + e.getMessage(), e);
        }
    }


    @Transactional
    public List<Animale> getAnimalsOfVeterinarian() {
        Utente veterinario = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario non trovato"));

        Long repartoId = veterinario.getReparto().getId();


        List<Utente> veterinariNelReparto = utenteRepository.findByDepartmentId(repartoId).stream()
                .filter(u -> "veterinario".equalsIgnoreCase(u.getRole()))
                .toList();

        return animaleRepository.findByVeterinarioIn(veterinariNelReparto);
    }

    @Transactional
    public List<Medicine> viewMedicineDepartment(Long departmentId) {
        List<Medicine> medicinali = medicineRepository.findByDepartmentId(departmentId);

        medicinali.forEach(medicinale -> {
            medicinale.setDescription(null);
        });

        return medicinali;
    }

    @Transactional
    public List<Veterinario> getVeterinariesByDepartment(Long departmentId) {

        return utenteRepository.findByDepartmentId(departmentId).stream()
                .filter(utente -> "veterinario".equalsIgnoreCase(utente.getRole()))
                .map(utente -> (Veterinario) utente)
                .collect(Collectors.toList());
    }


    @Transactional
    public Reparto getDepartmentByVeterinarian(String emailVeterinarian) {
        Utente veterinario = utenteRepository.findVeterinarioByEmail(emailVeterinarian)
                .orElseThrow(() -> new RuntimeException("Nessun veterinario trovato con email: " + emailVeterinarian));

        return veterinario.getReparto();
    }

    @Transactional
    public void expirationMedicine(Long headOfDepartmentId, Long medicineId){
        Utente dottore = utenteRepository.findByUsername(authenticationService.getUsername());
        if (dottore == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }
        Utente headOfDepartment = utenteRepository.findById(headOfDepartmentId).orElseThrow(() -> new IllegalArgumentException("Capo Reparto non trovato"));
        Medicine medicinale = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("Medicinale non trovato"));

        notificheService.sendVeterinarianNotificationToHeadOfDepartment(dottore,headOfDepartment, medicinale.getName());

    }

    public List<Veterinario> findByRepartoId(Long repartoId) {
        return veterinarioRepository.findByRepartoId(repartoId);
    }
}
