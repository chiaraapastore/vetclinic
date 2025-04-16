package com.vetclinic.controller;


import com.vetclinic.models.*;
import com.vetclinic.repository.EmergenzaRepository;
import com.vetclinic.repository.MagazzinoRepository;
import com.vetclinic.repository.RepartoRepository;
import com.vetclinic.repository.UtenteRepository;
import com.vetclinic.service.AdminService;
import com.vetclinic.service.KeycloakService;
import com.vetclinic.service.OrdineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/admin")
public class AdminController {


    private final AdminService adminService;
    private final RepartoRepository repartoRepository;
    private final UtenteRepository utenteRepository;
    private final KeycloakService keycloakService;
    private final MagazzinoRepository magazzinoRepository;
    private final EmergenzaRepository emergenzaRepository;
    private final OrdineService ordineService;

    public AdminController(AdminService adminService, RepartoRepository repartoRepository, UtenteRepository utenteRepository, EmergenzaRepository emergenzaRepository, KeycloakService keycloakService , OrdineService ordineService, MagazzinoRepository magazzinoRepository) {
        this.adminService = adminService;
        this.repartoRepository = repartoRepository;
        this.utenteRepository = utenteRepository;
        this.keycloakService = keycloakService;
        this.emergenzaRepository = emergenzaRepository;
        this.magazzinoRepository = magazzinoRepository;
        this.ordineService = ordineService;
    }


    @PostMapping("/create-department")
    public ResponseEntity<Map<String, String>> createDepartment(@RequestBody Map<String, String> payload) {
        String repartoNome = payload.get("repartoNome");

        if (repartoNome == null || repartoNome.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Il nome del reparto è obbligatorio."));
        }

        Optional<Reparto> existingReparto = repartoRepository.findFirstByNome(repartoNome);
        if (existingReparto.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Il reparto esiste già!"));
        }
        Reparto nuovoReparto = new Reparto();
        nuovoReparto.setName(repartoNome);
        repartoRepository.save(nuovoReparto);

        return ResponseEntity.ok(Map.of("message", "Reparto aggiunto con successo!"));
    }


    @PutMapping("/assign-head-of-department")
    public ResponseEntity<Map<String, String>> assignHeadOfDepartment(@RequestBody Map<String, Long> payload) {
        Long utenteId = payload.get("utenteId");
        Long repartoId = payload.get("repartoId");

        System.out.println("API ricevuta: Assegna capo reparto ID " + utenteId + " al reparto ID " + repartoId);

        if (utenteId == null || repartoId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Parametri mancanti."));
        }

        Optional<Utente> userOpt = utenteRepository.findById(utenteId);
        Optional<Reparto> departmentOpt = repartoRepository.findById(repartoId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Utente non trovato."));
        }

        if (departmentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Reparto non trovato."));
        }

        Utente utente = userOpt.get();
        Reparto nuovoReparto = departmentOpt.get();

        Optional<Reparto> repartoAttualeOpt = repartoRepository.findByCapoReparto(utente);
        repartoAttualeOpt.ifPresent(repartoAttuale -> {
            repartoAttuale.setHeadOfDepartment(null);
            repartoRepository.save(repartoAttuale);
        });

        nuovoReparto.setHeadOfDepartment(utente);
        utente.setReparto(nuovoReparto);
        utenteRepository.save(utente);
        repartoRepository.save(nuovoReparto);

        System.out.println("Capo reparto aggiornato con successo: " + utente.getFirstName() + " → " + nuovoReparto.getName());

        return ResponseEntity.ok(Map.of("message", "Capo reparto assegnato con successo!"));
    }


    @PutMapping("/assign-doctor-to-department/{utenteId}/{repartoId}")
    public ResponseEntity<Map<String, String>> assignDoctorToDepartment(@PathVariable Long utenteId, @PathVariable Long repartoId) {
        System.out.println("Ricevuta richiesta: Cambio reparto per dottore ID " + utenteId + " → Reparto ID " + repartoId);

        Optional<Utente> dottoreOpt = utenteRepository.findById(utenteId);
        Optional<Reparto> repartoOpt = repartoRepository.findById(repartoId);

        if (dottoreOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Dottore non trovato."));
        }

        if (repartoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Reparto non trovato."));
        }

        Utente dottore = dottoreOpt.get();
        Reparto reparto = repartoOpt.get();

        dottore.setReparto(reparto);
        utenteRepository.saveAndFlush(dottore);

        System.out.println("Dottore aggiornato al reparto: " + reparto.getName());

        return ResponseEntity.ok(Map.of("message", "Dottore assegnato al reparto " + reparto.getName()));
    }


    @GetMapping("/veterinaries")
    public ResponseEntity<List<VeterinarioDTO>> getAllVeterinaries() {
        return ResponseEntity.ok(adminService.getAllVeterinaries());
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Reparto>> getAllDepartments() {
        return ResponseEntity.ok(adminService.getAllDepartments());
    }

    @GetMapping("/head-of-department")
    public ResponseEntity<List<VeterinarioDTO>> getHeadOfDepartments() {
        return ResponseEntity.ok(adminService.getHeadOfDepartments());
    }


    @PostMapping("/create-veterinarian")
    public ResponseEntity<Map<String, String>> createVeterinarian(@RequestBody Map<String, String> payload) {
        String firstName = payload.get("firstName");
        String lastName = payload.get("lastName");
        String email = payload.get("email");
        String repartoNome = payload.get("repartoNome");


        if (firstName == null || lastName == null || email == null || repartoNome == null ||
                firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || repartoNome.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tutti i campi sono obbligatori, incluso il reparto."));
        }

        Optional<Reparto> repartoOpt = repartoRepository.findFirstByNome(repartoNome);
        if (repartoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Errore: Il reparto specificato non esiste."));
        }

        Reparto reparto = repartoOpt.get();


        Utente dottore = new Utente();
        dottore.setFirstName(firstName);
        dottore.setLastName(lastName);
        dottore.setEmail(email);
        dottore.setRole("dottore");
        dottore.setReparto(reparto);

        utenteRepository.save(dottore);

        return ResponseEntity.ok(Map.of("message", "Dottore creato con successo e assegnato al reparto " + reparto.getName()));

    }


    @PostMapping("/create-head-of-department")
    public ResponseEntity<Map<String, String>> createHeadOfDepartment(@RequestBody Map<String, String> payload) {
        System.out.println("Richiesta ricevuta: " + payload);

        String firstName = payload.get("firstName");
        String lastName = payload.get("lastName");
        String email = payload.get("email");
        String repartoNome = payload.get("repartoNome");

        if (firstName == null || lastName == null || email == null || repartoNome == null ||
                firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || repartoNome.isEmpty()) {
            System.out.println("Errore: Campi mancanti!");
            return ResponseEntity.badRequest().body(Map.of("error", "Tutti i campi sono obbligatori, incluso il reparto."));
        }

        Optional<Reparto> repartoOpt = repartoRepository.findFirstByNome(repartoNome);
        if (repartoOpt.isEmpty()) {
            System.out.println("Errore: Il reparto '" + repartoNome + "' non esiste!");
            return ResponseEntity.badRequest().body(Map.of("error", "Errore: Il reparto specificato non esiste."));
        }

        Reparto reparto = repartoOpt.get();
        String response = adminService.createHeadOfDepartment(firstName, lastName, email, reparto);
        return ResponseEntity.ok(Map.of("message", response));
    }


    @PostMapping("/add-medicinal")
    public ResponseEntity<Map<String, String>> addMedicinal(@RequestBody Map<String, Object> payload) {
        String result = adminService.addMedicine(payload);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @GetMapping("/warehouse")
    public ResponseEntity<List<Magazzino>> getAllWarehouses() {
        List<Magazzino> magazzini = magazzinoRepository.findAll();
        return ResponseEntity.ok(magazzini);
    }

    @GetMapping("/emergency")
    public ResponseEntity<List<Emergenza>> getEmergency(
            @RequestParam Long medicineId,
            @RequestParam Long animalId,
            @RequestParam Long veterinarianId) {

        adminService.checkAndCreateEmergencyForOutOfStockMedicine(medicineId, animalId, veterinarianId);

        List<Emergenza> emergenze = emergenzaRepository.findAll();
        return ResponseEntity.ok(emergenze);
    }


    @PostMapping("/create-assistant")
    public ResponseEntity<Map<String, String>> createAssistant(@RequestBody Map<String, Object> payload) {

        String firstName = (String) payload.get("firstName");
        String lastName = (String) payload.get("lastName");
        String registrationNumber = (String) payload.get("registrationNumber");
        Long repartoId = (Long) payload.get("repartoId");

        if (firstName == null || lastName == null || registrationNumber == null || repartoId == null ||
                firstName.isEmpty() || lastName.isEmpty() || registrationNumber.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tutti i campi sono obbligatori."));
        }

        try {
            String result = adminService.createAssistant(firstName, lastName, registrationNumber, repartoId);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/orders")
    public ResponseEntity<List<Ordine>> getOrdini() {
        return ResponseEntity.ok(adminService.getOrdini());
    }


    @GetMapping("/report-consumi")
    public ResponseEntity<List<Map<String, Object>>> getReportConsumi() {
        return ResponseEntity.ok(adminService.getReportConsumi());
    }

    @GetMapping("/emergencies/report")
    public ResponseEntity<List<Map<String, Object>>> getEmergencyReport() {
        List<Map<String, Object>> report = adminService.getEmergencyReport();
        return ResponseEntity.ok(report);
    }


    @PostMapping("/create-order")
    public ResponseEntity<Ordine> createOrder(@RequestBody Ordine ordine) {
        Ordine nuovoOrdine = ordineService.createOrder(ordine.getSupplier(), ordine.getQuantity());
        return ResponseEntity.ok(nuovoOrdine);
    }

    @GetMapping("/order-history")
    public List<Ordine> getOrderHistory() {
        return ordineService.getOrderHistory();
    }

    @GetMapping("/pending-order")
    public List<Ordine> getPendingOrders() {
        return ordineService.getPendingOrders();
    }

    @PutMapping("/order/{ordineId}/state")
    public ResponseEntity<Ordine> updateOrderState(@PathVariable Long ordineId, @RequestBody Map<String, String> payload) {
        Ordine.OrderStatus nuovoStato = Ordine.OrderStatus.valueOf(payload.get("stato"));
        Ordine ordineAggiornato = ordineService.updateOrderStatus(ordineId, nuovoStato);
        return ResponseEntity.ok(ordineAggiornato);
    }

    @PutMapping("/approve-holidays/{ferieId}")
    public ResponseEntity<Map<String, String>> approveHolidays(@PathVariable Long ferieId) {
        String result = adminService.approveHolidays(ferieId);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @DeleteMapping("/refuse-holidays/{ferieId}")
    public ResponseEntity<Map<String, String>> refuseHolidays(@PathVariable Long ferieId) {
        String result = adminService.refuseHolidays(ferieId);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @GetMapping("/approved-holidays")
    public ResponseEntity<Map<String, Object>> getApprovedHolidays() {
        return ResponseEntity.ok(Map.of("ferie", adminService.getApprovedHolidays()));
    }

    @GetMapping("/unapproved-holidays")
    public ResponseEntity<Map<String, Object>> getUnapprovedHolidays() {
        return ResponseEntity.ok(Map.of("ferie", adminService.getUnapprovedHolidays()));
    }

}

