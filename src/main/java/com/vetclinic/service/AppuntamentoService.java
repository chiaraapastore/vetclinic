package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.Appuntamento;
import com.vetclinic.models.Utente;
import com.vetclinic.repository.AppuntamentoRepository;
import com.vetclinic.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppuntamentoService {

    private final AppuntamentoRepository appuntamentoRepository;
    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;

    @Autowired
    public AppuntamentoService(AppuntamentoRepository appuntamentoRepository, UtenteRepository utenteRepository, AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
        this.appuntamentoRepository = appuntamentoRepository;
        this.utenteRepository = utenteRepository;
    }

    public Appuntamento createAppointment(Appuntamento appuntamento) {
        Utente authenticatedUtente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (authenticatedUtente == null) {
            throw new IllegalArgumentException("Utente autenticato non trovato");
        }
        return appuntamentoRepository.save(appuntamento);
    }

    public Optional<Appuntamento> getAppointmentById(Long id) {
        Utente authenticatedUtente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (authenticatedUtente == null) {
            throw new IllegalArgumentException("Utente autenticato non trovato");
        }
        return appuntamentoRepository.findById(id);
    }

    public List<Appuntamento> getAppointmentsByAnimal(Long animalId) {
        Utente authenticatedUtente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (authenticatedUtente == null) {
            throw new IllegalArgumentException("Utente autenticato non trovato");
        }
        return appuntamentoRepository.findByAnimalId(animalId);
    }

    public List<Appuntamento> getAppointmentsByVeterinarian(Long veterinarianId) {
        Utente authenticatedUtente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (authenticatedUtente == null) {
            throw new IllegalArgumentException("Utente autenticato non trovato");
        }
        return appuntamentoRepository.findByVeterinarianId(veterinarianId);
    }

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
        Utente authenticatedUtente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (authenticatedUtente == null) {
            throw new IllegalArgumentException("Utente autenticato non trovato");
        }
        appuntamentoRepository.deleteById(id);
    }
}
