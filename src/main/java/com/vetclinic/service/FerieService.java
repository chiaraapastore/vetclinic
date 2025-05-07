package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.Ferie;
import com.vetclinic.models.Utente;
import com.vetclinic.repository.FerieRepository;
import com.vetclinic.repository.UtenteRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class FerieService {

    private final FerieRepository ferieRepository;
    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;

    public FerieService(FerieRepository ferieRepository, UtenteRepository utenteRepository, AuthenticationService authenticationService) {
        this.ferieRepository = ferieRepository;
        this.utenteRepository = utenteRepository;
        this.authenticationService = authenticationService;
    }

    @Transactional
    public List<Ferie> getHolidaysForUser(Long utenteId) {
        return ferieRepository.findByUtenteId(utenteId);
    }

    @Transactional
    public List<Ferie> getHolidaysApproved() {
        return ferieRepository.findByApprovedTrue();
    }

    @Transactional
    public List<Ferie> getHolidaysNotApproved() {
        return ferieRepository.findByApprovedFalse();
    }

    @Transactional
    public String addHolidays(Long utenteId, LocalDate startDate, LocalDate endDate) {
        Utente user = utenteRepository.findByUsername(authenticationService.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }

        if (!(user.getRole().equalsIgnoreCase("veterinario") || user.getRole().equalsIgnoreCase("assistente") || user.getRole().equalsIgnoreCase("capo-reparto"))) {
            throw new IllegalArgumentException("Ruolo non autorizzato per richiedere ferie.");
        }


        Optional<Utente> utenteOpt = utenteRepository.findById(utenteId);
        if (utenteOpt.isEmpty()) {
            return "Utente non trovato.";
        }

        Utente utente = utenteOpt.get();

        Ferie ferie = new Ferie();
        ferie.setUtente(utente);
        ferie.setStartDate(startDate);
        ferie.setEndDate(endDate);
        ferie.setApproved(false);

        ferieRepository.save(ferie);
        return "Ferie richieste con successo.";
    }

    @Transactional
    public String approveHolidays(Long ferieId) {
        Utente loggedUser = utenteRepository.findByUsername(authenticationService.getUsername());
        if (loggedUser == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }

        if (!loggedUser.getRole().equals("ADMIN") && !loggedUser.getRole().equals("CAPO_REPARTO")) {
            throw new IllegalArgumentException("Non hai il permesso di approvare ferie.");
        }

        Optional<Ferie> ferieOpt = ferieRepository.findById(ferieId);
        if (ferieOpt.isEmpty()) {
            return "Ferie non trovate.";
        }

        Ferie ferie = ferieOpt.get();
        ferie.setApproved(true);
        ferieRepository.save(ferie);

        return "Ferie approvate con successo.";
    }

    @Transactional
    public String refuseHolidays(Long ferieId) {
        Utente loggedUser = utenteRepository.findByUsername(authenticationService.getUsername());
        if (loggedUser == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }

        if (!loggedUser.getRole().equals("ADMIN") && !loggedUser.getRole().equals("CAPO_REPARTO")) {
            throw new IllegalArgumentException("Non hai il permesso di rifiutare ferie.");
        }

        Optional<Ferie> ferieOpt = ferieRepository.findById(ferieId);
        if (ferieOpt.isEmpty()) {
            return "Ferie non trovate.";
        }

        ferieRepository.delete(ferieOpt.get());

        return "Ferie rifiutate e cancellate.";
    }


    public Long getUserIdByKeycloak() {
        Utente utente = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));
        return utente.getId();
    }

    @Transactional
    public List<Ferie> getHolidaysForUserInRange(Long utenteId, LocalDate start, LocalDate end) {
        return ferieRepository.findByUtenteIdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(utenteId, start, end);
    }


}
