package com.vetclinic.service;


import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
                .map(utente -> (Veterinario) utente)
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
                .map(utente -> (Veterinario) utente)
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

    public String createDepartment(Map<String, String> payload) {
        String repartoName = payload.get("repartoNome");

        if (repartoName == null || repartoName.isEmpty()) {
            throw new IllegalArgumentException("Il nome del reparto è obbligatorio.");
        }

        Optional<Reparto> existingReparto = repartoRepository.findFirstByName(repartoName);
        if (existingReparto.isPresent()) {
            throw new IllegalArgumentException("Il reparto esiste già!");
        }
        Reparto nuovoReparto = new Reparto();
        nuovoReparto.setName(repartoName);
        repartoRepository.save(nuovoReparto);

        return "Reparto aggiunto con successo!";
    }

    public String assignHeadOfDepartment(Map<String, Long> payload) {
        Long utenteId = payload.get("utenteId");
        Long repartoId = payload.get("repartoId");

        System.out.println("API ricevuta: Assegna capo reparto ID " + utenteId + " al reparto ID " + repartoId);

        if (utenteId == null || repartoId == null) {
            throw new IllegalArgumentException("Parametri mancanti.");
        }

        Optional<Utente> userOpt = utenteRepository.findById(utenteId);
        Optional<Reparto> departmentOpt = repartoRepository.findById(repartoId);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Utente non trovato.");
        }

        if (departmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Reparto non trovato.");
        }

        Utente utente = userOpt.get();
        Reparto nuovoReparto = departmentOpt.get();

        if (!(utente instanceof CapoReparto)) {
            throw new IllegalArgumentException("L'utente non è un capo reparto.");
        }

        CapoReparto capoReparto = (CapoReparto) utente;

        Optional<Reparto> repartoAttualeOpt = repartoRepository.findByCapoRepartoId(utente.getId());
        repartoAttualeOpt.ifPresent(repartoAttuale -> {
            repartoRepository.save(repartoAttuale);
        });

        capoReparto.setReparto(nuovoReparto);
        utenteRepository.save(capoReparto);
        repartoRepository.save(nuovoReparto);

        System.out.println("Capo reparto aggiornato con successo: " + capoReparto.getFirstName() + " → " + nuovoReparto.getName());

        return "Capo reparto assegnato con successo!";
    }

    public String assignDoctorToDepartment(Long utenteId, Long repartoId) {
        System.out.println("Ricevuta richiesta: Cambio reparto per dottore ID " + utenteId + " → Reparto ID " + repartoId);
        Reparto reparto = repartoRepository.findById(repartoId).orElseThrow(()-> new IllegalArgumentException("Reparto non trovato"));

        Utente dottore = utenteRepository.findById(utenteId).orElseThrow(()-> new IllegalArgumentException("Utente non trovato"));

        if(!dottore.getRole().equals("veterinarian")) {
            throw new IllegalArgumentException("L'utente non è un veterinarian.");
        }
        dottore.setReparto(reparto);
        utenteRepository.saveAndFlush(dottore);

        System.out.println("Dottore aggiornato al reparto: " + reparto.getName());

        return "Dottore assegnato al reparto " + reparto.getName();
    }

    public String createCliente(Map<String, String> payload) {
        String username= payload.get("username");
        String firstName = payload.get("firstName");
        String lastName = payload.get("lastName");
        String email = payload.get("email");
        String phoneNumber = payload.get("phoneNumber");
        String address = payload.get("address");

        if (firstName == null || lastName == null || email == null ||
                phoneNumber == null || address == null ||
                firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || address.isEmpty()) {
            throw new IllegalArgumentException("Tutti i campi sono obbligatori.");
        }

        Cliente cliente = new Cliente();
        cliente.setFirstName(firstName);
        cliente.setLastName(lastName);
        cliente.setEmail(email);
        cliente.setPhoneNumber(phoneNumber);
        cliente.setUsername(username);
        cliente.setRole("cliente");

        try {
            keycloakService.createUser(cliente);
            clienteRepository.save(cliente);
        } catch (Exception e) {
            throw new RuntimeException("Errore nella creazione del cliente su Keycloak o nel salvataggio nel database: " + e.getMessage());
        }
        return "Cliente creato con successo!";
    }

    public String createVeterinarian(Map<String, String> payload) {
        String username = payload.get("username");
        String firstName = payload.get("firstName");
        String registration_number = payload.get("registration_number");
        String lastName = payload.get("lastName");
        String email = payload.get("email");
        String repartoName = payload.get("repartoNome");


        if (firstName == null || lastName == null || email == null || repartoName == null ||
                firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || repartoName.isEmpty()) {
            throw new IllegalArgumentException("Tutti i campi sono obbligatori, incluso il reparto.");
        }

        Optional<Reparto> repartoOpt = repartoRepository.findFirstByName(repartoName);
        if (repartoOpt.isEmpty()) {
            throw new IllegalArgumentException("Errore: Il reparto specificato non esiste.");
        }

        Reparto reparto = repartoOpt.get();


        Utente dottore = new Utente();
        dottore.setFirstName(firstName);
        dottore.setRegistrationNumber(registration_number);
        dottore.setLastName(lastName);
        dottore.setEmail(email);
        dottore.setRole("veterinario");
        dottore.setUsername(username);
        dottore.setReparto(reparto);

        try {
            keycloakService.createUser(dottore);
            utenteRepository.save(dottore);
        } catch (Exception e) {
            throw new RuntimeException("Errore nella creazione del veterinario su Keycloak o nel salvataggio nel database: " + e.getMessage());
        }

        return "Dottore creato con successo e assegnato al reparto " + reparto.getName();
    }
}
