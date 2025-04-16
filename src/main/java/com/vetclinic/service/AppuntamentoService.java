package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.Appuntamento;
import com.vetclinic.models.Utente;
import com.vetclinic.repository.AppuntamentoRepository;
import com.vetclinic.repository.UtenteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppuntamentoService {

    private final AppuntamentoRepository appuntamentoRepository;
    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;

    public AppuntamentoService(AppuntamentoRepository appuntamentoRepository, UtenteRepository utenteRepository, AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
        this.appuntamentoRepository = appuntamentoRepository;
        this.utenteRepository = utenteRepository;
    }

    @Transactional
    public Appuntamento createAppointment(Appuntamento appuntamento) {
        Utente authenticatedUtente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (authenticatedUtente == null) {
            throw new IllegalArgumentException("Utente autenticato non trovato");
        }
        return appuntamentoRepository.save(appuntamento);
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

    @Transactional
    public void deleteAppointment(Long id) {
        Utente authenticatedUtente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (authenticatedUtente == null) {
            throw new IllegalArgumentException("Utente autenticato non trovato");
        }
        appuntamentoRepository.deleteById(id);
    }
}
