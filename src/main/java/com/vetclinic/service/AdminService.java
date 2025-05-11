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
    private final VeterinarioRepository veterinarioRepository;

    public AdminService(UtenteRepository utenteRepository, VeterinarioRepository veterinarioRepository,KeycloakService keycloakService,RepartoRepository repartoRepository, FerieRepository ferieRepository, AuthenticationService authenticationService, AnimaleRepository animaleRepository, AssistenteRepository assistenteRepository, MedicineRepository medicineRepository, OrdineRepository ordineRepository, MagazzinoRepository magazzinoRepository, NotificheService notificheService, ClienteRepository clienteRepository) {
        this.repartoRepository = repartoRepository;
        this.authenticationService = authenticationService;
        this.utenteRepository = utenteRepository;
        this.animaleRepository = animaleRepository;
        this.medicineRepository = medicineRepository;
        this.assistenteRepository = assistenteRepository;
        this.magazineRepository = magazzinoRepository;
        this.ordineRepository = ordineRepository;
        this.notificationService = notificheService;
        this.ferieRepository = ferieRepository;
        this.clienteRepository = clienteRepository;
        this.keycloakService = keycloakService;
        this.veterinarioRepository = veterinarioRepository;
    }



    @Transactional
    public List<Veterinario> getAllVeterinaries() {
        return veterinarioRepository.findAll();
    }



    private String generateRegistrationNumber(String prefix) {
        int randomNum = new Random().nextInt(1000);
        return prefix + String.format("%03d", randomNum);
    }


    @Transactional
    public List<Reparto> getAllDepartments() {
        return repartoRepository.findAll();
    }


    @Transactional
    public List<CapoReparto> getHeadOfDepartments() {
        return utenteRepository.findAllCapoReparto();
    }



    @Transactional
    public String createHeadOfDepartment(Map<String, String> payload) {
        Utente utenteAdmin = utenteRepository.findByUsername(authenticationService.getUsername());
        if (utenteAdmin == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }

        String username = payload.get("username");
        String firstName = payload.get("firstName");
        String lastName = payload.get("lastName");
        String email = payload.get("email");
        String repartoName = payload.get("repartoNome");
        String registrationNumber = payload.getOrDefault("registration_number", "");

        if (username == null || firstName == null || lastName == null || email == null || repartoName == null ||
                registrationNumber == null ||username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || repartoName.isEmpty() || registrationNumber.isEmpty()) {
            throw new IllegalArgumentException("Tutti i campi sono obbligatori, incluso il reparto.");
        }

        if (registrationNumber.isEmpty()) {
            registrationNumber = generateRegistrationNumber("CAP");
        }

        Reparto reparto = repartoRepository.findFirstByName(repartoName)
                .orElseThrow(() -> new IllegalArgumentException("Errore: Il reparto specificato non esiste."));

        CapoReparto capoReparto = new CapoReparto();
        capoReparto.setUsername(username);
        capoReparto.setFirstName(firstName);
        capoReparto.setLastName(lastName);
        capoReparto.setEmail(email);
        capoReparto.setRole("capo-reparto");
        capoReparto.setReparto(reparto);
        capoReparto.setRegistrationNumber(registrationNumber);

        try {
            keycloakService.createUser(capoReparto);
            utenteRepository.save(capoReparto);

            reparto.setCapoRepartoId(capoReparto.getId());
            repartoRepository.save(reparto);

            notificationService.sendWelcomeNotification(utenteAdmin, capoReparto);
        } catch (Exception e) {
            throw new RuntimeException("Errore nella creazione del capo reparto su Keycloak o nel salvataggio nel database: " + e.getMessage());
        }

        return "Capo Reparto creato con successo e assegnato al reparto " + reparto.getName();
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
    public String createAssistant(Map<String, String> payload) {
        String username = payload.get("username");
        String firstName = payload.get("firstName");
        String lastName = payload.get("lastName");
        String email = payload.get("email");
        String repartoName = payload.get("repartoName");
        String registrationNumber = payload.getOrDefault("registration_number", "");

        if (username == null || firstName == null || lastName == null || email == null || repartoName == null ||
                registrationNumber == null ||username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || repartoName.isEmpty() || registrationNumber.isEmpty()) {
            throw new IllegalArgumentException("Tutti i campi sono obbligatori, incluso il reparto.");
        }
        if (registrationNumber.isEmpty()) {
            registrationNumber = generateRegistrationNumber("ASS");
        }

        Reparto reparto = repartoRepository.findFirstByName(repartoName)
                .orElseThrow(() -> new IllegalArgumentException("Reparto non trovato"));

        Assistente assistente = new Assistente();
        assistente.setUsername(username);
        assistente.setFirstName(firstName);
        assistente.setLastName(lastName);
        assistente.setEmail(email);
        assistente.setRole("assistente");
        assistente.setReparto(reparto);
        assistente.setRegistrationNumber(registrationNumber);

        try {
            keycloakService.createUser(assistente);
            utenteRepository.save(assistente);
        } catch (Exception e) {
            throw new RuntimeException("Errore nella creazione dell'assistente su Keycloak o nel salvataggio nel database: " + e.getMessage());
        }

        return "Assistente " + firstName + " " + lastName + " creato con successo e assegnato al reparto " + reparto.getName();
    }






    @Transactional
    public List<Ordine> getOrdini() {
        return ordineRepository.findAll();
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


        return "Capo reparto assegnato con successo!";
    }

    public String assignDoctorToDepartment(Long utenteId, Long repartoId) {
          Reparto reparto = repartoRepository.findById(repartoId).orElseThrow(()-> new IllegalArgumentException("Reparto non trovato"));

        Utente dottore = utenteRepository.findById(utenteId).orElseThrow(()-> new IllegalArgumentException("Utente non trovato"));

        if(!dottore.getRole().equals("veterinario")) {
            throw new IllegalArgumentException("L'utente non è un veterinarian.");
        }
        dottore.setReparto(reparto);
        utenteRepository.saveAndFlush(dottore);


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

    @Transactional
    public String createVeterinarian(Map<String, String> payload) {
        String username = payload.get("username");
        String firstName = payload.get("firstName");
        String registrationNumber = payload.getOrDefault("registration_number", "");
        String lastName = payload.get("lastName");
        String email = payload.get("email");
        String repartoName = payload.get("repartoNome");

        if (firstName == null || lastName == null || email == null || repartoName == null ||
                registrationNumber == null||firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || repartoName.isEmpty()|| registrationNumber.isEmpty()) {
            throw new IllegalArgumentException("Tutti i campi sono obbligatori, incluso il reparto.");
        }

        if (registrationNumber.isEmpty()) {
            registrationNumber = generateRegistrationNumber("VET");
        }

        Reparto reparto = repartoRepository.findFirstByName(repartoName)
                .orElseThrow(() -> new IllegalArgumentException("Errore: Il reparto specificato non esiste."));

        Veterinario dottore = new Veterinario();
        dottore.setFirstName(firstName);
        dottore.setLastName(lastName);
        dottore.setEmail(email);
        dottore.setUsername(username);
        dottore.setRegistrationNumber(registrationNumber);
        dottore.setRole("veterinario");
        dottore.setReparto(reparto);

        try {
            keycloakService.createUser(dottore);
            utenteRepository.save(dottore);
        } catch (Exception e) {
            throw new RuntimeException("Errore nella creazione del veterinario su Keycloak o nel salvataggio nel database: " + e.getMessage());
        }

        return "Dottore creato con successo e assegnato al reparto " + reparto.getName();
    }

    public List<Assistente> getAllAssistants() {
        return assistenteRepository.findAll();
    }

    @Transactional
    public String eliminaUtente(Long utenteId) {
        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato."));

        try {

            notificationService.eliminaNotifichePerUtente(utente);


            keycloakService.deleteUser(utente.getUsername());

            utenteRepository.delete(utente);

            return "Utente eliminato con successo.";
        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'eliminazione dell'utente: " + e.getMessage());
        }
    }




}
