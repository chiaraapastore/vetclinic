package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.Animale;
import com.vetclinic.models.Appuntamento;
import com.vetclinic.models.Utente;
import com.vetclinic.models.Veterinario;
import com.vetclinic.repository.AnimaleRepository;
import com.vetclinic.repository.AppuntamentoRepository;
import com.vetclinic.repository.UtenteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AppuntamentoService {

    private final AppuntamentoRepository appuntamentoRepository;
    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;
    private final AnimaleRepository animaleRepository;


    public AppuntamentoService(AppuntamentoRepository appuntamentoRepository, UtenteRepository utenteRepository, AuthenticationService authenticationService, AnimaleRepository animaleRepository) {
        this.authenticationService = authenticationService;
        this.appuntamentoRepository = appuntamentoRepository;
        this.utenteRepository = utenteRepository;
        this.animaleRepository = animaleRepository;
    }


    @Transactional
    public Optional<Appuntamento> getAppointmentById(Long id) {
        Utente authenticatedUtente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (authenticatedUtente == null) {
            throw new IllegalArgumentException("Utente autenticato non trovato");
        }
        return appuntamentoRepository.findById(id);
    }

    @Transactional
    public List<Appuntamento> getAppointmentsByAnimal(Long animalId) {
        Utente authenticatedUtente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (authenticatedUtente == null) {
            throw new IllegalArgumentException("Utente autenticato non trovato");
        }
        return appuntamentoRepository.findByAnimalId(animalId);
    }

    @Transactional
    public List<Appuntamento> getAppointmentsByVeterinarian(Long veterinarianId) {
        Utente authenticatedUtente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (authenticatedUtente == null) {
            throw new IllegalArgumentException("Utente autenticato non trovato");
        }
        return appuntamentoRepository.findByVeterinarianId(veterinarianId);
    }

    @Transactional
    public Appuntamento updateAppointment(Long id, Appuntamento appuntamentoDetails) {
        Utente authenticatedUtente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (authenticatedUtente == null) {
            throw new IllegalArgumentException("Utente autenticato non trovato");
        }
        Appuntamento appuntamento = appuntamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appuntamento non trovato con ID: " + id));

        appuntamento.setAppointmentDate(appuntamentoDetails.getAppointmentDate());
        appuntamento.setReason(appuntamentoDetails.getReason());

        return appuntamentoRepository.save(appuntamento);
    }

    public void deleteAppointment(Long id) {
        Appuntamento appointment = appuntamentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appuntamento non trovato"));

        appuntamentoRepository.delete(appointment);
    }

}
