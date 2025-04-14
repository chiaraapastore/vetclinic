package com.vetclinic.controller;


import com.vetclinic.models.*;
import com.vetclinic.service.AssistenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/assistente")
public class AssistenteController {

    private final AssistenteService assistenteService;

    @Autowired
    public AssistenteController(AssistenteService assistenteService) {
        this.assistenteService = assistenteService;
    }


    @PostMapping("/create-appointment")
    public ResponseEntity<Appuntamento> createAppointment(
            @RequestParam Long animalId,
            @RequestParam Long veterinarianId,
            @RequestParam Date appointmentDate,
            @RequestParam String reason) {
        Appuntamento appointment = assistenteService.createAppointment(animalId, veterinarianId, appointmentDate, reason);
        return ResponseEntity.ok(appointment);
    }


    @DeleteMapping("/delete-appointment/{appointmentId}")
    public ResponseEntity<String> deleteAppointment(@PathVariable Long appointmentId) {
        assistenteService.deleteAppointment(appointmentId);
        return ResponseEntity.ok("Appointment successfully canceled.");
    }

    @PostMapping("/remind-appointment/{appointmentId}")
    public ResponseEntity<String> remindAppointment(@PathVariable Long appointmentId) {
        assistenteService.remindAppointment(appointmentId);
        return ResponseEntity.ok("Reminder successfully sent.");
    }


    @PostMapping("/check-medicine-expiration")
    public ResponseEntity<String> checkMedicineExpiration(@RequestParam Long departmentHeadId, @RequestParam Long medicineId) {
        assistenteService.checkMedicineExpiration(departmentHeadId, medicineId);
        return ResponseEntity.ok("Medicine expiration check completed.");
    }


    @GetMapping("/view-department-medicines/{departmentId}")
    public ResponseEntity<List<Medicine>> viewDepartmentMedicines(@PathVariable Long departmentId) {
        List<Medicine> medicines = assistenteService.viewDepartmentMedicines(departmentId);
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
    public ResponseEntity<List<VeterinarioDTO>> getVeterinariesByDepartment(@PathVariable Long departmentId) {
        List<VeterinarioDTO> veterinaries = assistenteService.getVeterinariesByDepartment(departmentId);
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

}
