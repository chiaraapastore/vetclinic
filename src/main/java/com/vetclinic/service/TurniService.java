package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.Turni;
import com.vetclinic.models.Utente;
import com.vetclinic.repository.TurniRepository;
import com.vetclinic.repository.UtenteRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TurniService {

    private final TurniRepository turniRepository;
    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;
    private final NotificheService notificheService;

    public TurniService(TurniRepository turniRepository, UtenteRepository utenteRepository, AuthenticationService authenticationService, NotificheService notificheService) {
        this.turniRepository = turniRepository;
        this.utenteRepository = utenteRepository;
        this.authenticationService = authenticationService;
        this.notificheService = notificheService;
    }

    @Transactional
    public Turni assignTurno(Long dottoreId, LocalDate inizioTurno, LocalDate fineTurno) {
        Utente loggedUser = utenteRepository.findByUsername(authenticationService.getUsername());
        if (loggedUser == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }

        if (!loggedUser.getRole().equals("admin") && !loggedUser.getRole().equals("capo-reparto")) {
            throw new IllegalArgumentException("Non hai il permesso di assegnare i turni.");
        }

        Utente dottore = utenteRepository.findById(dottoreId).orElseThrow(() -> new IllegalArgumentException("Dottore non trovato"));
        if (loggedUser.getRole().equals("capo-reparto") && !dottore.getRole().equals("veterinario")) {
            throw new IllegalArgumentException("Un capo reparto può assegnare turni solo ai veterinari.");
        }

        Turni turno = new Turni(inizioTurno, fineTurno, loggedUser);
        notificheService.sendNotificationTurni(dottore, loggedUser, inizioTurno, fineTurno);
        return turniRepository.save(turno);
    }

    @Transactional
    public List<Turni> getShiftsForUser(Long utenteId) {
        return turniRepository.findByUtenteId(utenteId);
    }

    @Transactional
    public List<Turni> getShiftsApproved() {
        return turniRepository.findByApprovedTrue();
    }

    @Transactional
    public List<Turni> getShiftsNotApproved() {
        return turniRepository.findByApprovedFalse();
    }

    @Transactional
    public String addShift(Long utenteId, LocalDate  startTime, LocalDate endTime) {
        Utente user = utenteRepository.findByUsername(authenticationService.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }

        Utente dottore = utenteRepository.findById(utenteId).orElseThrow(() -> new IllegalArgumentException("Dottore non trovato"));
        if (user.getRole().equals("capo-reparto") && !dottore.getRole().equals("veterinario")) {
            throw new IllegalArgumentException("Un capo reparto può assegnare turni solo ai veterinari.");
        }


        if (!user.getRole().equals("veterinario") && !user.getRole().equals("assistente")) {
            throw new IllegalArgumentException("Non hai il permesso di aggiungere un turno.");
        }

        Turni turno = new Turni();
        turno.setUtente(user);
        turno.setStartTime(startTime);
        turno.setEndTime(endTime);
        turno.setApproved(false);

        turniRepository.save(turno);
        return "Turno richiesto con successo.";
    }

    @Transactional
    public String approveShift(Long turnoId) {
        Optional<Turni> turnoOpt = turniRepository.findById(turnoId);
        if (turnoOpt.isEmpty()) {
            return "Turno non trovato.";
        }

        Turni turno = turnoOpt.get();
        turno.setApproved(true);
        turniRepository.save(turno);

        return "Turno approvato con successo.";
    }

    @Transactional
    public String refuseShift(Long turnoId) {
        Optional<Turni> turnoOpt = turniRepository.findById(turnoId);
        if (turnoOpt.isEmpty()) {
            return "Turno non trovato.";
        }

        turniRepository.delete(turnoOpt.get());

        return "Turno rifiutato e cancellato.";
    }
}
