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
    private final OrdineService ordineService;
    private final AnimaleService animaleService;
    private final ClienteRepository clienteRepository;

    public AdminController(AdminService adminService, ClienteRepository   clienteRepository,RepartoRepository repartoRepository, UtenteRepository utenteRepository, KeycloakService keycloakService , OrdineService ordineService, MagazzinoRepository magazzinoRepository, AnimaleService animaleService) {
        this.adminService = adminService;
        this.repartoRepository = repartoRepository;
        this.utenteRepository = utenteRepository;
        this.keycloakService = keycloakService;
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
    public ResponseEntity<List<CapoReparto>> getHeadOfDepartments() {
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
        try {
            String message = adminService.createHeadOfDepartment(payload);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Errore interno: " + e.getMessage()));
        }
    }

    @GetMapping("/assistants")
    public ResponseEntity<List<Assistente>> getAllAssistants() {
        return ResponseEntity.ok(adminService.getAllAssistants());
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



    @PostMapping("/create-assistant")
    public ResponseEntity<Map<String, String>> createAssistant(@RequestBody Map<String, String> payload) {
        try {
            String message = adminService.createAssistant(payload);
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


    @PostMapping("/create-order")
    public ResponseEntity<Ordine> createOrder(@RequestBody Ordine ordine) {
        Ordine nuovoOrdine = ordineService.createOrder(ordine.getSupplierName(), ordine.getQuantity());
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

