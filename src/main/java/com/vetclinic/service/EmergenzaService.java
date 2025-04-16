package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.Emergenza;
import com.vetclinic.models.Utente;
import com.vetclinic.repository.EmergenzaRepository;
import com.vetclinic.repository.UtenteRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmergenzaService {

    private final EmergenzaRepository emergenzaRepository;
    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;

    public EmergenzaService(EmergenzaRepository emergenzaRepository,  UtenteRepository utenteRepository, AuthenticationService authenticationService) {
        this.emergenzaRepository = emergenzaRepository;
        this.utenteRepository = utenteRepository;
        this.authenticationService = authenticationService;
    }

    @Transactional
    public String solveEmergency(Long emergencyId) {
        Utente utente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (utente == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }
        Optional<Emergenza> emergencyOpt = emergenzaRepository.findById(emergencyId);
        if (emergencyOpt.isEmpty()) {
            return "Emergenza non trovata.";
        }

        Emergenza emergency = emergencyOpt.get();
        emergenzaRepository.delete(emergency);
        return "Emergenza risolta e rimossa con successo.";
    }
}
