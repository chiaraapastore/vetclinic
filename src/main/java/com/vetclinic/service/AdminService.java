package com.vetclinic.service;


import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service

public class AdminService {

    private final UtenteRepository utenteRepository;
    private final RepartoRepository repartoRepository;
    public final EmergenzaRepository emergenzaRepository;
    private final FerieRepository ferieRepository;
    private final AnimaleRepository animaleRepository;
    private final MedicineRepository medicineRepository;
    private final MagazzinoRepository magazineRepository;
    private final OrdineRepository ordineRepository;
    private final AssistenteRepository assistenteRepository;
    private final AuthenticationService authenticationService;
    private final NotificheService notificationService;
    private final ClienteRepository clienteRepository;
    private final KeycloakService keycloakService;

    public AdminService(UtenteRepository utenteRepository, KeycloakService keycloakService,RepartoRepository repartoRepository, FerieRepository ferieRepository, AuthenticationService authenticationService, AnimaleRepository animaleRepository, AssistenteRepository assistenteRepository, MedicineRepository medicineRepository, OrdineRepository ordineRepository, MagazzinoRepository magazzinoRepository, NotificheService notificheService, ClienteRepository clienteRepository,EmergenzaRepository emergenzaRepository) {
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
        this.ferieRepository = ferieRepository;
        this.clienteRepository = clienteRepository;
        this.keycloakService = keycloakService;
    }



    @Transactional
    public List<Veterinario> getAllVeterinaries() {
        return utenteRepository.findAll().stream()
                .filter(utente -> "veterinario".equalsIgnoreCase(utente.getRole()))
                .map(utente -> new Veterinario(
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
    public List<Veterinario> getHeadOfDepartments() {
        return utenteRepository.findAll().stream()
                .filter(utente -> "capo-reparto".equalsIgnoreCase(utente.getRole()))
                .map(utente -> new Veterinario(
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


        Veterinario veterinarian = utenteRepository.findById(veterinarianId)
                .map(utente -> (Veterinario) utente)
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
    public String createAssistant(String username, String firstName, String lastName, String email, String registrationNumber, String repartoName) {

        Reparto reparto = repartoRepository.findFirstByName(repartoName)
                .orElseThrow(() -> new IllegalArgumentException("Reparto non trovato"));

        Assistente assistente = new Assistente();
        assistente.setUsername(username);
        assistente.setFirstName(firstName);
        assistente.setLastName(lastName);
        assistente.setEmail(email);
        assistente.setRegistrationNumber(registrationNumber);
        assistente.setRole("assistente");
        assistente.setReparto(reparto);

        keycloakService.createUser(assistente);
        utenteRepository.save(assistente);


        reparto.setAssistente(assistente);
        repartoRepository.save(reparto);

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


    @Transactional
    public String approveHolidays(Long ferieId) {
        Optional<Ferie> ferieOpt = ferieRepository.findById(ferieId);
        if (ferieOpt.isEmpty()) {
            return "Ferie non trovate.";
        }

        Ferie ferie = ferieOpt.get();
        ferie.setApproved(true);
        ferieRepository.save(ferie);

        return "Ferie approvate con successo.";
    }

    @Transactional
    public String refuseHolidays(Long ferieId) {
        Optional<Ferie> ferieOpt = ferieRepository.findById(ferieId);
        if (ferieOpt.isEmpty()) {
            return "Ferie non trovate.";
        }

        ferieRepository.delete(ferieOpt.get());

        return "Ferie rifiutate e cancellate.";
    }


    @Transactional
    public List<Ferie> getApprovedHolidays() {
        return ferieRepository.findByApprovedTrue();
    }


    @Transactional
    public List<Ferie> getUnapprovedHolidays() {
        return ferieRepository.findByApprovedFalse();
    }

    @Transactional
    public String assignAssistantToDepartment(Long utenteId, Long repartoId) {
        Optional<Utente> assistenteOpt = utenteRepository.findById(utenteId);
        Optional<Reparto> repartoOpt = repartoRepository.findById(repartoId);

        if (assistenteOpt.isEmpty()) {
            throw new IllegalArgumentException("Assistente non trovato.");
        }

        if (repartoOpt.isEmpty()) {
            throw new IllegalArgumentException("Reparto non trovato.");
        }

        Utente assistente = assistenteOpt.get();
        Reparto reparto = repartoOpt.get();


        assistente.setReparto(reparto);
        utenteRepository.saveAndFlush(assistente);

        System.out.println("Assistente aggiornato al reparto: " + reparto.getName());

        return "Assistente assegnato al reparto " + reparto.getName();
    }

    @Transactional
    public String createAnimalForClient(Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String species = (String) payload.get("species");
        String breed = (String) payload.get("breed");
        Integer age = (Integer) payload.get("age");
        String state = (String) payload.get("state");
        String microchip = (String) payload.get("microchip");
        String veterinaryNotes = (String) payload.get("veterinaryNotes");
        String nextVisit = (String) payload.get("nextVisit");
        String symptoms = (String) payload.get("symptoms");
        Double weight = payload.get("weight") != null ? Double.valueOf(payload.get("weight").toString()) : null;
        Long clienteId = Long.valueOf(payload.get("clienteId").toString());
        Long veterinarioId = Long.valueOf(payload.get("veterinarioId").toString());

        Utente utenteCliente = utenteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato"));

        Utente utenteVeterinario = utenteRepository.findById(veterinarioId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinario non trovato"));

        if (!(utenteCliente instanceof Cliente)) {
            throw new IllegalArgumentException("L'utente non è un cliente valido");
        }
        if (!(utenteVeterinario instanceof Veterinario)) {
            throw new IllegalArgumentException("L'utente non è un veterinario valido");
        }

        Cliente cliente = (Cliente) utenteCliente;
        Veterinario veterinario = (Veterinario) utenteVeterinario;

        Animale animale = new Animale();
        animale.setName(name);
        animale.setSpecies(species);
        animale.setBreed(breed);
        animale.setAge(age != null ? age : 0);
        animale.setState(state);
        animale.setMicrochip(microchip);
        animale.setVeterinaryNotes(veterinaryNotes);
        animale.setNextVisit(nextVisit);
        animale.setSymptoms(symptoms);
        animale.setWeight(weight != null ? weight : 0.0);
        animale.setCliente(cliente);
        animale.setVeterinario(veterinario);

        animaleRepository.save(animale);

        System.out.println("Animale creato: " + animale.getName() + " per il cliente " + cliente.getFirstName());

        return "Animale creato con successo: " + animale.getName();
    }




}
