package com.vetclinic.controller;


import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import com.vetclinic.service.*;
import org.springframework.http.HttpStatus;
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
    private final AnimaleService animaleService;
    private final ClienteRepository clienteRepository;

    public AdminController(AdminService adminService, ClienteRepository   clienteRepository,RepartoRepository repartoRepository, UtenteRepository utenteRepository, EmergenzaRepository emergenzaRepository, KeycloakService keycloakService , OrdineService ordineService, MagazzinoRepository magazzinoRepository, AnimaleService animaleService) {
        this.adminService = adminService;
        this.repartoRepository = repartoRepository;
        this.utenteRepository = utenteRepository;
        this.keycloakService = keycloakService;
        this.emergenzaRepository = emergenzaRepository;
        this.magazzinoRepository = magazzinoRepository;
        this.ordineService = ordineService;
        this.animaleService = animaleService;
        this.clienteRepository = clienteRepository;
    }



    @PostMapping("/create-department")
    public ResponseEntity<Map<String, String>> createDepartment(@RequestBody Map<String, String> payload) {
        try {
            String response = adminService.createDepartment(payload);
            return ResponseEntity.ok(Map.of("message", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/assign-head-of-department")
    public ResponseEntity<Map<String, String>> assignHeadOfDepartment(@RequestBody Map<String, Long> payload) {
        try {
            String response = adminService.assignHeadOfDepartment(payload);
            return ResponseEntity.ok(Map.of("message", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/assign-doctor-to-department/{utenteId}/{repartoId}")
    public ResponseEntity<Map<String, String>> assignDoctorToDepartment(@PathVariable Long utenteId, @PathVariable Long repartoId) {
        try {
            String message = adminService.assignDoctorToDepartment(utenteId, repartoId);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Errore interno: " + e.getMessage()));
        }
    }



    @PostMapping("/assign-assistant-to-department/{utenteId}/{repartoId}")
    public ResponseEntity<Map<String, String>> assignAssistantToDepartment(@PathVariable Long utenteId, @PathVariable Long repartoId) {
        System.out.println("Ricevuta richiesta: Cambio reparto per assistente ID " + utenteId + " → Reparto ID " + repartoId);

        try {
            String message = adminService.assignAssistantToDepartment(utenteId, repartoId);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    @PostMapping("/create-animale")
    public ResponseEntity<Map<String, String>> createAnimalForClient(@RequestBody Map<String, Object> payload) {
        String result = animaleService.createAnimalForClient(payload);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/create-cliente")
    public ResponseEntity<Object> createCliente(@RequestBody Map<String, String> payload) {
        try {
            String message = adminService.createCliente(payload);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Errore interno: " + e.getMessage()));
        }
    }


    @GetMapping("/veterinaries")
    public ResponseEntity<List<Veterinario>> getAllVeterinaries() {
        return ResponseEntity.ok(adminService.getAllVeterinaries());
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Reparto>> getAllDepartments() {
        return ResponseEntity.ok(adminService.getAllDepartments());
    }

    @GetMapping("/head-of-department")
    public ResponseEntity<List<Veterinario>> getHeadOfDepartments() {
        return ResponseEntity.ok(adminService.getHeadOfDepartments());
    }


    @PostMapping("/create-veterinarian")
    public ResponseEntity<Map<String, String>> createVeterinarian(@RequestBody Map<String, String> payload) {
        try {
            String message = adminService.createVeterinarian(payload);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Errore interno: " + e.getMessage()));
        }
    }



    @PostMapping("/create-head-of-department")
    public ResponseEntity<Map<String, String>> createHeadOfDepartment(@RequestBody Map<String, String> payload) {
        System.out.println("Richiesta ricevuta: " + payload);

        String firstName = payload.get("firstName");
        String lastName = payload.get("lastName");
        String email = payload.get("email");
        String repartoName = payload.get("repartoNome");

        if (firstName == null || lastName == null || email == null || repartoName == null ||
                firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || repartoName.isEmpty()) {
            System.out.println("Errore: Campi mancanti!");
            return ResponseEntity.badRequest().body(Map.of("error", "Tutti i campi sono obbligatori, incluso il reparto."));
        }

        Optional<Reparto> repartoOpt = repartoRepository.findFirstByName(repartoName);
        if (repartoOpt.isEmpty()) {
            System.out.println("Errore: Il reparto '" + repartoName + "' non esiste!");
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
    public ResponseEntity<Map<String, String>> createAssistant(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String firstName = payload.get("firstName");
        String lastName = payload.get("lastName");
        String email = payload.get("email");
        String registrationNumber = payload.get("registrationNumber");
        String repartoName = payload.get("repartoName");

        if (username == null || firstName == null || lastName == null || email == null || repartoName == null ||
                username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || repartoName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tutti i campi sono obbligatori, incluso il reparto."));
        }

        try {
            String message = adminService.createAssistant(username, firstName, lastName, email, registrationNumber, repartoName);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Errore interno: " + e.getMessage()));
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

