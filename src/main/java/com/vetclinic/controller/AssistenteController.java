package com.vetclinic.controller;


import com.vetclinic.models.*;
import com.vetclinic.repository.AppuntamentoRepository;
import com.vetclinic.service.AdminService;
import com.vetclinic.service.AppuntamentoService;
import com.vetclinic.service.AssistenteService;
import com.vetclinic.service.OrdineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/assistente")
public class AssistenteController {

    private final AssistenteService assistenteService;
    private final AdminService adminService;
    private final OrdineService ordineService;
    private final AppuntamentoRepository appuntamentoRepository;

    @Autowired
    public AssistenteController(AssistenteService assistenteService, AppuntamentoRepository appuntamentoRepository,OrdineService ordineService, AdminService adminService) {
        this.assistenteService = assistenteService;
        this.ordineService = ordineService;
        this.adminService = adminService;
        this.appuntamentoRepository = appuntamentoRepository;
    }

    @PostMapping("/create-appointment")
    public ResponseEntity<Appuntamento> createAppointment(
            @RequestParam Long animalId,
            @RequestParam Long veterinarianId,
            @RequestParam String appointmentDate,
            @RequestParam String reason,
            @RequestParam Double amount) {

        try {
            LocalDateTime localDateTime = LocalDateTime.parse(appointmentDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

            Appuntamento appointment = assistenteService.createAppointment(animalId, veterinarianId, date, reason, amount);
            return ResponseEntity.ok(appointment);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato data non valido: " + appointmentDate);
        }
    }

    @PostMapping("/aggiungi-farmaco")
    public ResponseEntity<Map<String, String>> aggiungiFarmaco(@RequestBody Map<String, Object> payload) {
        String result = assistenteService.aggiungiFarmaco(payload);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<List<Appuntamento>> getMyAppointments() {
        List<Appuntamento> appointments = assistenteService.getAppointmentsForRepartoOfAssistant();
        return ResponseEntity.ok(appointments);
    }


    @GetMapping("/list-order")
    public ResponseEntity<List<Ordine>> getOrdini() {
        return ResponseEntity.ok(adminService.getOrdini());
    }

    @PostMapping("/create-order")
    public ResponseEntity<Ordine> createOrder(@RequestBody Map<String, Object> ordineData) {
        String supplierName = (String) ordineData.get("supplier");
        int quantity = (Integer) ordineData.get("quantity");

        Ordine nuovoOrdine = ordineService.createOrder(supplierName, quantity);
        return ResponseEntity.ok(nuovoOrdine);
    }


    @GetMapping("/order-history")
    public List<Ordine> getOrderHistory() {
        return ordineService.getOrderHistory();
    }


    @DeleteMapping("/delete-appointment/{appointmentId}")
    public ResponseEntity<String> deleteAppointment(@PathVariable Long appointmentId) {
        assistenteService.deleteAppointment(appointmentId);
        return ResponseEntity.ok("Appointment successfully canceled.");
    }

    @PostMapping("/remind-appointment/{appointmentId}")
    public ResponseEntity<String> remindAppointment(
            @PathVariable Long appointmentId,
            @RequestParam String newDate) {
        assistenteService.remindAppointment(appointmentId, newDate);
        return ResponseEntity.ok("Rimandato correttamente");
    }


    @PostMapping("/check-medicine-expiration")
    public ResponseEntity<String> checkMedicineExpiration(@RequestParam Long departmentHeadId, @RequestParam Long medicineId) {
        assistenteService.checkMedicineExpiration(departmentHeadId, medicineId);
        return ResponseEntity.ok("Medicine expiration check completed.");
    }


    @GetMapping("/view-department-medicines/{departmentId}")
    public ResponseEntity<List<MedicineDTO>> viewDepartmentMedicines(@PathVariable Long departmentId) {
        List<MedicineDTO> medicines = assistenteService.viewDepartmentMedicines(departmentId);
        return ResponseEntity.ok(medicines);
    }



    @GetMapping("/get-veterinarian-patients")
    public ResponseEntity<List<Animale>> getVeterinarianPatients() {
        List<Animale> patients = assistenteService.getVeterinarianPatients();
        return ResponseEntity.ok(patients);
    }


    @GetMapping("/get-reparto-by-dottore")
    public ResponseEntity<String> getRepartoByDottore(@RequestParam String emailVeterinarian) {
        String departmentName = assistenteService.getRepartoByVeterinarian(emailVeterinarian).getName();
        return ResponseEntity.ok("Reparto: " + departmentName);
    }

    @GetMapping("/get-veterinaries-by-department/{departmentId}")
    public ResponseEntity<List<Veterinario>> getVeterinariesByDepartment(@PathVariable Long departmentId) {
        List<Veterinario> veterinaries = assistenteService.getVeterinariesByDepartment(departmentId);
        return ResponseEntity.ok(veterinaries);
    }

    @GetMapping("/get-assistenti-by-reparto/{repartoId}")
    public ResponseEntity<List<Assistente>> getAssistentiByReparto(@PathVariable Long repartoId) {
        List<Assistente> assistenti = assistenteService.getAssistentiByReparto(repartoId);
        if (assistenti.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(assistenti);
    }

    @GetMapping("/get-assistenti-by-name")
    public ResponseEntity<List<Assistente>> getAssistentiByName(
            @RequestParam String firstName,
            @RequestParam String lastName) {
        List<Assistente> assistenti = assistenteService.getAssistentiByName(firstName, lastName);
        return ResponseEntity.ok(assistenti);
    }

    @GetMapping("/get-all-assistenti")
    public ResponseEntity<List<Assistente>> getAllAssistenti() {
        List<Assistente> assistenti = assistenteService.getAllAssistenti();
        return ResponseEntity.ok(assistenti);
    }

    @GetMapping("/get-assistente/{id}")
    public ResponseEntity<Assistente> getAssistenteById(@PathVariable Long id) {
        Assistente assistente = assistenteService.getAssistenteById(id);
        return ResponseEntity.ok(assistente);
    }

    @GetMapping("/emergenze")
    public ResponseEntity<List<Medicine>> getEmergenze() {
        return ResponseEntity.ok(assistenteService.getEmergenze());
    }


    @PostMapping("/somministra-farmaco")
    public ResponseEntity<Map<String, String>> administerMedicine(
            @RequestParam Long animaleId,
            @RequestParam Long medicineId,
            @RequestParam int quantita,
            @RequestParam Long veterinarianId
    ) {
        String result = assistenteService.administerMedicine(animaleId, medicineId, quantita, veterinarianId);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PutMapping("/ordini/{ordineId}/stato")
    public ResponseEntity<Ordine> aggiornaStatoOrdine(
            @PathVariable Long ordineId,
            @RequestBody Map<String, String> body) {

        try {
            String statoStr = body.get("stato");
            Ordine.OrderStatus nuovoStato = Ordine.OrderStatus.valueOf(statoStr.toUpperCase());

            Ordine aggiornato = ordineService.aggiornaStatoOrdine(ordineId, nuovoStato);
            return ResponseEntity.ok(aggiornato);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stato non valido: deve essere PENDING, COMPLETED o CANCELED");
        }
    }


    @PostMapping("/scadenza")
    public ResponseEntity<?> scadenzaFarmaco(@RequestBody Map<String, Long> body) {
        Long capoRepartoId = body.get("capoRepartoId");
        Long idMedicinale = body.get("medicinaleId");
        assistenteService.scadenzaFarmaco(capoRepartoId, idMedicinale);
        return ResponseEntity.ok(Map.of("message", "notifica_inviata"));
    }


    @PutMapping("/update-amount/{id}")
    public ResponseEntity<Map<String, String>> updateAppointmentAmount(
            @PathVariable Long id,
            @RequestParam Double amount) {
        try {
            Appuntamento appointment = appuntamentoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appuntamento non trovato"));
            appointment.setAmount(amount);
            appuntamentoRepository.save(appointment);

            return ResponseEntity.ok(Map.of("message", "Importo aggiornato con successo"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore aggiornando importo"));
        }
    }


    @GetMapping("/richieste-appuntamenti/non-approvate")
    public ResponseEntity<List<RichiestaAppuntamento>> getRichiesteNonApprovate() {
        List<RichiestaAppuntamento> richieste = assistenteService.getRichiesteNonApprovate();
        return ResponseEntity.ok(richieste);
    }

    @PostMapping("/richieste-appuntamenti/approva/{id}")
    public ResponseEntity<Map<String, String>> approvaRichiesta(@PathVariable Long id) {
        assistenteService.approvaRichiestaAppuntamento(id);
        return ResponseEntity.ok(Map.of("message", "Richiesta approvata e appuntamento creato."));
    }




    @PostMapping("/richieste-appuntamenti/rifiuta/{id}")
    public ResponseEntity<Map<String, String>> rifiutaRichiesta(@PathVariable Long id) {
        assistenteService.rifiutaRichiestaAppuntamento(id);
        return ResponseEntity.ok(Map.of("message", "Richiesta rifiutata."));
    }
















}
