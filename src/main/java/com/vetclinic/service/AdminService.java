package com.vetclinic.service;


import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service

public class AdminService {

    private final UtenteRepository utenteRepository;
    private final RepartoRepository repartoRepository;
    public final EmergenzaRepository emergenzaRepository;

    private final AnimaleRepository animaleRepository;
    private final MedicineRepository medicineRepository;
    private final MagazzinoRepository magazineRepository;
    private final OrdineRepository ordineRepository;

    private final AssistenteRepository assistenteRepository;
    private final AuthenticationService authenticationService;
    private final NotificheService notificationService;

    public AdminService(UtenteRepository utenteRepository, RepartoRepository repartoRepository, AuthenticationService authenticationService, AnimaleRepository animaleRepository, AssistenteRepository assistenteRepository, MedicineRepository medicineRepository, OrdineRepository ordineRepository, MagazzinoRepository magazzinoRepository, NotificheService notificheService, EmergenzaRepository emergenzaRepository) {
        this.repartoRepository = repartoRepository;
        this.authenticationService = authenticationService;
        this.utenteRepository = utenteRepository;
        this.animaleRepository = animaleRepository;
        this.medicineRepository = medicineRepository;
        this.assistenteRepository = assistenteRepository;
        this.magazineRepository = magazzinoRepository;
        this.ordineRepository = ordineRepository;
        this.notificationService = notificheService;
        this.emergenzaRepository = emergenzaRepository;
    }



    @Transactional
    public List<VeterinarioDTO> getAllVeterinaries() {
        return utenteRepository.findAll().stream()
                .filter(utente -> "veterinario".equalsIgnoreCase(utente.getRole()))
                .map(utente -> new VeterinarioDTO(
                        utente.getId(),
                        utente.getFirstName(),
                        utente.getLastName(),
                        utente.getEmail(),
                        utente.getRegistrationNumber(),
                        utente.getReparto() != null ? utente.getReparto().getName() : "Nessun reparto"
                ))
                .collect(Collectors.toList());
    }



    @Transactional
    public List<Reparto> getAllDepartments() {
        return repartoRepository.findAll();
    }


    @Transactional
    public List<VeterinarioDTO> getHeadOfDepartments() {
        return utenteRepository.findAll().stream()
                .filter(utente -> "capo-reparto".equalsIgnoreCase(utente.getRole()))
                .map(utente -> new VeterinarioDTO(
                        utente.getId(),
                        utente.getFirstName(),
                        utente.getLastName(),
                        utente.getEmail(),
                        utente.getRegistrationNumber(),
                        (utente.getReparto() != null) ? utente.getReparto().getName() : "Nessun reparto"
                ))
                .collect(Collectors.toList());
    }


    @Transactional
    public String createHeadOfDepartment(String firstName, String lastName, String email, Reparto department) {
        Utente utenteAdmin = utenteRepository.findByUsername(authenticationService.getUsername());
        if (utenteAdmin == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }
        Utente capoReparto = new Utente();
        capoReparto.setFirstName(firstName);
        capoReparto.setLastName(lastName);
        capoReparto.setEmail(email);
        capoReparto.setRole("capoReparto");
        capoReparto.setReparto(department);
        utenteRepository.save(capoReparto);
        notificationService.sendWelcomeNotification(utenteAdmin, capoReparto);
        return "Capo Reparto creato con successo e assegnato al reparto " + department.getName();
    }


    @Transactional
    public String addMedicine(Map<String, Object> payload) {
        String name = (String) payload.get("nome");
        Integer quantity = (Integer) payload.get("quantita");
        String dosaggio = (String) payload.get("dosagem");
        String scadenza = (String) payload.get("scadenza");
        Integer availableQuantity = (Integer) payload.get("availableQuantity");
        String effetti = (String) payload.get("effetti-collaterali");
        String descrizione = (String) payload.get("descrizione");
        Long departmentId = payload.get("departmentId") != null ? Long.valueOf(payload.get("departmentId").toString()) : null;
        Long magazineId = payload.get("magazineId") != null ? Long.valueOf(payload.get("magazineId").toString()) : null;

        if (name == null || name.isEmpty() || quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Nome del farmaco e quantità devono essere validi.");
        }

        Medicine newMedicine = new Medicine();
        newMedicine.setName(name);
        newMedicine.setQuantity(quantity);
        newMedicine.setExpirationDate(scadenza);
        newMedicine.setDescription(descrizione);
        newMedicine.setDosage(dosaggio);
        newMedicine.setAvailableQuantity(availableQuantity);
        newMedicine.setSideEffects(effetti);


        if (departmentId != null) {
            Reparto department = repartoRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Dipartimento non trovato"));
            newMedicine.setDepartment(department);
        }

        if (magazineId != null) {
            Magazzino magazzino = magazineRepository.findById(magazineId)
                    .orElseThrow(() -> new IllegalArgumentException("Magazzino non trovato"));
            newMedicine.setMagazzino(magazzino);
        }

        medicineRepository.save(newMedicine);
        return "Farmaco aggiunto con successo!";
    }


    @Transactional
    public void checkAndCreateEmergencyForOutOfStockMedicine(Long medicineId, Long animalId, Long veterinarianId) {

        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("Medicinale non trovato"));

        Animale animal = animaleRepository.findById(animalId)
                .orElseThrow(() -> new IllegalArgumentException("Animale non trovato"));


        VeterinarioDTO veterinarian = utenteRepository.findById(veterinarianId)
                .map(utente -> (VeterinarioDTO) utente)
                .orElseThrow(() -> new IllegalArgumentException("Veterinario non trovato"));


        if (medicine.getAvailableQuantity() <= 0) {
            Emergenza emergenza = new Emergenza();
            emergenza.setAnimal(animal);
            emergenza.setVeterinarian(veterinarian);
            emergenza.setEmergencyDate(new Date());
            emergenza.setDescription("Emergenza: Il farmaco " + medicine.getName() + " è esaurito.");
            emergenza.setMedicine(medicine);
            emergenza.setDosage(medicine.getDosage());

            emergenzaRepository.save(emergenza);
        }
    }


    @Transactional
    public String createAssistant(String firstName, String lastName, String registrationNumber, Long repartoId) {
        Utente utenteAdmin = utenteRepository.findByUsername(authenticationService.getUsername());
        if (utenteAdmin == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }

        Reparto reparto = repartoRepository.findById(repartoId)
                .orElseThrow(() -> new IllegalArgumentException("Reparto non trovato"));

        Assistente nuovoAssistente = new Assistente();
        nuovoAssistente.setFirstName(firstName);
        nuovoAssistente.setLastName(lastName);
        nuovoAssistente.setRegistrationNumber(registrationNumber);
        nuovoAssistente.setReparto(reparto);


        assistenteRepository.save(nuovoAssistente);

        return "Assistente " + firstName + " " + lastName + " creato con successo e assegnato al reparto " + reparto.getName();
    }

    @Transactional
    public List<Ordine> getOrdini() {
        return ordineRepository.findAll();
    }


    @Transactional
    public List<Map<String, Object>> getEmergencyReport() {
        return emergenzaRepository.findAll().stream()
                .map(emergenza -> {
                    Map<String, Object> reportMap = new HashMap<>();
                    reportMap.put("emergencyId", emergenza.getId());
                    reportMap.put("animalName", emergenza.getAnimal().getName());
                    reportMap.put("veterinarianName", emergenza.getVeterinarian().getFirstName() + " " + emergenza.getVeterinarian().getLastName());
                    reportMap.put("emergencyDate", emergenza.getEmergencyDate());
                    reportMap.put("medicine", emergenza.getMedicine().getName());
                    reportMap.put("dosage", emergenza.getDosage());
                    reportMap.put("description", emergenza.getDescription());
                    return reportMap;
                })
                .collect(Collectors.toList());
    }



    @Transactional
    public List<Map<String, Object>> getReportConsumi() {
        return medicineRepository.findConsumoPerReparto();
    }


}
