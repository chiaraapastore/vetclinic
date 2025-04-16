package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final EmergenzaRepository emergenzaRepository;
    private final SomministrazioneRepository somministrazioneRepository;
    private final NotificheService notificheService;
    private final OperazioneRepository operazioneRepository;


    public VeterinarianService(RepartoRepository repartoRepository, OperazioneRepository operazioneRepository, MedicineRepository medicineRepository, AnimaleRepository animaleRepository, AuthenticationService authenticationService, UtenteRepository utenteRepository, EmergenzaRepository emergenzaRepository, SomministrazioneRepository  somministrazioneRepository, NotificheService notificheService) {
        this.repartoRepository = repartoRepository;
        this.medicineRepository = medicineRepository;
        this.animaleRepository = animaleRepository;
        this.authenticationService = authenticationService;
        this.utenteRepository = utenteRepository;
        this.emergenzaRepository = emergenzaRepository;
        this.somministrazioneRepository = somministrazioneRepository;
        this.notificheService = notificheService;
        this.operazioneRepository = operazioneRepository;

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

            Operazione operazione = new Operazione();
            operazione.setAnimale(animale);
            operazione.setVeterinario(utente);
            operazione.setTipoOperazione(tipoOperazione);
            operazione.setDescrizione(descrizioneOperazione);
            operazione.setDataOra(LocalDateTime.now());

            operazioneRepository.save(operazione);

            animale.setState("Operato");
            animaleRepository.save(animale);

            return "Operazione eseguita con successo per il paziente " + animale.getName();

        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'esecuzione dell'operazione: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Somministrazione administersMedicines(Long pazienteId, Long capoRepartoId, String nomeMedicinale, int quantita) {
        try {
            Utente utente = utenteRepository.findByUsername(authenticationService.getUsername());
            if (utente == null) {
                throw new IllegalArgumentException("Utente non trovato");
            }

            Animale animale = animaleRepository.findById(pazienteId)
                    .orElseThrow(() -> new RuntimeException("Paziente non trovato"));

            Medicine medicine = medicineRepository.findByName(nomeMedicinale)
                    .orElseThrow(() -> new RuntimeException("Medicinale non trovato"));

            Utente capoReparto = utenteRepository.findById(capoRepartoId)
                    .orElseThrow(() -> new RuntimeException("Capo Reparto non trovato"));

            if (medicine.getAvailableQuantity() <= 0) {
                throw new RuntimeException("Il medicinale non è più disponibile");
            }

            if (medicine.getAvailableQuantity() < quantita) {
                throw new RuntimeException("Quantità insufficiente in magazzino!");
            }

            medicine.setAvailableQuantity(medicine.getAvailableQuantity() - quantita);
            medicine.setQuantity(medicine.getQuantity() - quantita);

            if (medicine.getQuantity() <= 0) {
                medicineRepository.delete(medicine);
            } else {
                medicineRepository.save(medicine);
            }

            Somministrazione somministrazione = new Somministrazione();
            somministrazione.setAnimal(animale);
            somministrazione.setMedicine(medicine);
            somministrazione.setDosage(quantita);
            somministrazione.setDate(LocalDateTime.now());
            somministrazioneRepository.save(somministrazione);

            notificheService.sendNotificationSomministration(utente, capoReparto, nomeMedicinale);

            return somministrazione;

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la somministrazione: " + e.getMessage(), e);
        }
    }


    @Transactional
    public String reportEmergency(Long animaleId, Long veterinarianId, Long medicineId, String description, String dosage) {
        Optional<Utente> utenteOpt = utenteRepository.findById(veterinarianId);
        if (utenteOpt.isEmpty()) {
            return "Veterinario non trovato.";
        }

        VeterinarioDTO veterinarian = (VeterinarioDTO) utenteOpt.get();
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicinale non trovato"));

        if (medicine.getAvailableQuantity() <= 0) {
            return "Il farmaco richiesto non è disponibile.";
        }

        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new RuntimeException("Animale non trovato"));

        Emergenza emergenza = new Emergenza();
        emergenza.setAnimal(animale);
        emergenza.setVeterinarian(veterinarian);
        emergenza.setEmergencyDate(new Date());
        emergenza.setDescription(description);
        emergenza.setMedicine(medicine);
        emergenza.setDosage(dosage);

        emergenzaRepository.save(emergenza);

        notificheService.sendEmergencyNotificationToHeadOfDepartment(veterinarian, veterinarian.getReparto().getHeadOfDepartment(), description, medicine);


        Utente assistant = utenteRepository.findByUsername(authenticationService.getUsername());
        if (assistant == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }

        notificheService.sendEmergencyNotificationToAssistant(veterinarian, assistant, description, medicine);


        return "Emergenza segnalata con successo.";
    }


    @Transactional
    public List<Animale> getAnimalsOfVeterinarian() {
        String username = authenticationService.getUsername();
        Utente veterinarian = utenteRepository.findByUsername(username);
        if (veterinarian == null) {
            throw new IllegalArgumentException("Dottore non trovato");
        }

        List<Animale> animali = animaleRepository.findByVeterinario(veterinarian);
        System.out.println("Pazienti del dottore " + username + ": " + animali);
        return animali;
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
    public List<VeterinarioDTO> getVeterinariesByDepartment(Long departmentId) {
        return utenteRepository.findByDepartmentId(departmentId).stream()
                .map(dottore -> new VeterinarioDTO(
                        dottore.getId(),
                        dottore.getFirstName(),
                        dottore.getLastName(),
                        dottore.getEmail(),
                        dottore.getRegistrationNumber(),
                        dottore.getReparto().getName()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public Reparto getDepartmentByVeterinarian(String emailVeterinarian) {
        return utenteRepository.findRepartoByEmailVeterinarian(emailVeterinarian)
                .orElseThrow(() -> new RuntimeException("Nessun veterinario trovato con email: " + emailVeterinarian));
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

}
