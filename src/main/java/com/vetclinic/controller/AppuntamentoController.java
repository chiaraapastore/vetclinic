package com.vetclinic.controller;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.Appuntamento;
import com.vetclinic.models.Cliente;
import com.vetclinic.models.Utente;
import com.vetclinic.models.Veterinario;
import com.vetclinic.repository.AppuntamentoRepository;
import com.vetclinic.repository.UtenteRepository;
import com.vetclinic.service.AppuntamentoService;
import com.vetclinic.service.AssistenteService;
import com.vetclinic.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/appuntamenti")
public class AppuntamentoController {

    private final AppuntamentoService appuntamentoService;
    private final AssistenteService assistenteService;
    private final UtenteRepository utenteRepository;
    private final AppuntamentoRepository appuntamentoRepository;
    private final AuthenticationService authenticationService;
    private final ClienteService clienteService;

    public AppuntamentoController(AppuntamentoService appuntamentoService, ClienteService clienteService, AssistenteService assistenteService, UtenteRepository utenteRepository, AppuntamentoRepository appuntamentoRepository, AuthenticationService authenticationService) {
        this.appuntamentoService = appuntamentoService;
        this.utenteRepository = utenteRepository;
        this.appuntamentoRepository = appuntamentoRepository;
        this.authenticationService = authenticationService;
        this.assistenteService = assistenteService;
        this.clienteService = clienteService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<Appuntamento>> getMyAppointments() {
        Utente veterinario = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario non trovato"));
        List<Appuntamento> appointments = appuntamentoRepository.findByVeterinarianId(veterinario.getId());
        return ResponseEntity.ok(appointments);
    }



    @GetMapping("/veterinario/assistente-appuntamenti")
    public ResponseEntity<List<Appuntamento>> getAppointmentsCreatedByAssistantForVeterinarian() {
        Utente veterinario = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario non trovato"));

        List<Appuntamento> appointments = appuntamentoRepository.findByVeterinarianId(veterinario.getId());
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/cliente/miei-appuntamenti")
    public ResponseEntity<List<Appuntamento>> getAppointmentsForCliente() {
        Cliente cliente = clienteService.getClienteAutenticato();
        List<Appuntamento> appointments = appuntamentoService.getAppointmentsForClient(cliente.getId());
        return ResponseEntity.ok(appointments);
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
