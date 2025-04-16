package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.Ferie;
import com.vetclinic.models.Medicine;
import com.vetclinic.models.Reparto;
import com.vetclinic.models.Utente;
import com.vetclinic.repository.FerieRepository;
import com.vetclinic.repository.MedicineRepository;
import com.vetclinic.repository.RepartoRepository;
import com.vetclinic.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CapoRepartoService {



    private MedicineRepository medicineRepository;
    private final AuthenticationService authenticationService;
    private final UtenteRepository utenteRepository;
    private final NotificheService notificheService;
    private final RepartoRepository repartoRepository;
    private final FerieRepository ferieRepository;

    public CapoRepartoService(MedicineRepository medicineRepository, UtenteRepository utenteRepository, FerieRepository ferieRepository,AuthenticationService authenticationService, RepartoRepository repartoRepository, NotificheService notificheService) {
        this.medicineRepository = medicineRepository;
        this.authenticationService = authenticationService;
        this.utenteRepository = utenteRepository;
        this.repartoRepository = repartoRepository;
        this.notificheService = notificheService;
        this.ferieRepository = ferieRepository;
    }


    @Transactional
    public void changeDepartment(Long doctorId, Long nuovoRepartoId) {
        Utente dottore = utenteRepository.findByUsername(authenticationService.getUsername());
        if (dottore== null) {
            throw new IllegalArgumentException("Dottore non autenticato");
        }

        Reparto reparto = repartoRepository.findById(nuovoRepartoId).orElseThrow(() -> new IllegalArgumentException("Reparto non trovato"));
        dottore.setReparto(reparto);
        utenteRepository.save(dottore);
        notificheService.notifyDepartmentChange(dottore, reparto.getName(), dottore);
    }



    @Transactional
    public List<Reparto> getDepartments() {
        Utente user = utenteRepository.findByUsername(authenticationService.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }
        return repartoRepository.findAll();
    }



    @Transactional
    public String addMedicinal(Medicine medicine) {
        Utente user = utenteRepository.findByUsername(authenticationService.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }
        Reparto reparto = repartoRepository.findById(medicine.getDepartment().getId())
                .orElseThrow(() -> new IllegalArgumentException("Reparto non trovato"));

        Medicine nuovoMedicinale = new Medicine();
        nuovoMedicinale.setName(medicine.getName());
        nuovoMedicinale.setQuantity(medicine.getQuantity());
        nuovoMedicinale.setDepartment(reparto);

        medicineRepository.save(nuovoMedicinale);
        return "Medicinale " + medicine.getName() + " aggiunto con successo al reparto " + reparto.getName();
    }

    public String addHolidaysForReparto(Long utenteId, LocalDate startDate, LocalDate endDate) {
        Optional<Utente> utenteOpt = utenteRepository.findById(utenteId);
        if (utenteOpt.isEmpty()) {
            return "Utente non trovato.";
        }
        Utente utente = utenteOpt.get();

        Utente capoReparto  = utenteRepository.findByUsername(authenticationService.getUsername());
        if (capoReparto  == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }
        if (!utente.getReparto().equals(capoReparto.getReparto())) {
            return "L'utente non appartiene al reparto del capo reparto.";
        }

        Ferie ferie = new Ferie();
        ferie.setUtente(utente);
        ferie.setStartDate(startDate);
        ferie.setEndDate(endDate);
        ferie.setApproved(false);

        ferieRepository.save(ferie);
        return "Ferie richieste con successo.";
    }

    @Transactional
    public String approveHolidaysForReparto(Long ferieId) {
        Optional<Ferie> ferieOpt = ferieRepository.findById(ferieId);
        if (ferieOpt.isEmpty()) {
            return "Ferie non trovate.";
        }

        Ferie ferie = ferieOpt.get();


        Utente capoReparto  = utenteRepository.findByUsername(authenticationService.getUsername());
        if (capoReparto  == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }
        if (!ferie.getUtente().getReparto().equals(capoReparto.getReparto())) {
            return "Non puoi approvare le ferie per un utente di un altro reparto.";
        }

        ferie.setApproved(true);
        ferieRepository.save(ferie);

        return "Ferie approvate con successo.";
    }

    @Transactional
    public List<Ferie> getHolidaysForReparto(Long repartoId) {
        return ferieRepository.findByUtenteRepartoIdAndApprovedTrue(repartoId);
    }

    @Transactional
    public List<Ferie> getUnapprovedHolidaysForReparto(Long repartoId) {
        return ferieRepository.findByUtenteRepartoIdAndApprovedFalse(repartoId);
    }

}
