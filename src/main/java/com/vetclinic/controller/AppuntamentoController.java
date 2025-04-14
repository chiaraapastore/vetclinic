package com.vetclinic.controller;

import com.vetclinic.models.Appuntamento;
import com.vetclinic.service.AppuntamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/appuntamenti")
public class AppuntamentoController {

    private final AppuntamentoService appuntamentoService;

    @Autowired
    public AppuntamentoController(AppuntamentoService appuntamentoService) {
        this.appuntamentoService = appuntamentoService;
    }

    @PostMapping("/create-appointment")
    public ResponseEntity<Appuntamento> createAppointment(@RequestBody Appuntamento appointment) {
        Appuntamento createdAppointment = appuntamentoService.createAppointment(appointment);
        return ResponseEntity.ok(createdAppointment);
    }

    @GetMapping("/list-appointment/{id}")
    public ResponseEntity<Appuntamento> getAppointment(@PathVariable Long id) {
        Optional<Appuntamento> appointment = appuntamentoService.getAppointmentById(id);
        return appointment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/animal/{animalId}")
    public ResponseEntity<List<Appuntamento>> getAppointmentsByAnimal(@PathVariable Long animalId) {
        List<Appuntamento> appointments = appuntamentoService.getAppointmentsByAnimal(animalId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/veterinario/{veterinarioId}")
    public ResponseEntity<List<Appuntamento>> getAppointmentsByVeterinarian(@PathVariable Long veterinarianId) {
        List<Appuntamento> appointments = appuntamentoService.getAppointmentsByVeterinarian(veterinarianId);
        return ResponseEntity.ok(appointments);
    }

    @PutMapping("update-appointment/{id}")
    public ResponseEntity<Appuntamento> updateAppointment(@PathVariable Long id, @RequestBody Appuntamento appointmentDetails) {
        Appuntamento updatedAppointment = appuntamentoService.updateAppointment(id, appointmentDetails);
        return ResponseEntity.ok(updatedAppointment);
    }


    @DeleteMapping("delete-appointment/{id}")
    public ResponseEntity<Map<String, String>> deleteAppointment(@PathVariable Long id) {
        appuntamentoService.deleteAppointment(id);
        return ResponseEntity.ok(Map.of("message", "Appuntamento cancellato con successo"));
    }
}
