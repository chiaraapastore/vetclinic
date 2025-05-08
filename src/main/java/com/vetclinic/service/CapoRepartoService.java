package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    private final AnimaleRepository animaleRepository;
    private final ReportRepository reportRepository;

    public CapoRepartoService(MedicineRepository medicineRepository, ReportRepository reportRepository,AnimaleRepository animaleRepository,UtenteRepository utenteRepository, FerieRepository ferieRepository,AuthenticationService authenticationService, RepartoRepository repartoRepository, NotificheService notificheService) {
        this.medicineRepository = medicineRepository;
        this.authenticationService = authenticationService;
        this.utenteRepository = utenteRepository;
        this.repartoRepository = repartoRepository;
        this.notificheService = notificheService;
        this.ferieRepository = ferieRepository;
        this.animaleRepository = animaleRepository;
        this.reportRepository = reportRepository;
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
        Utente user = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato");
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

    @Transactional
    public String addHolidaysForReparto(Long utenteId, LocalDate startDate, LocalDate endDate) {

        Optional<Utente> optionalUtente = utenteRepository.findById(utenteId);

        if (optionalUtente.isEmpty()) {
            return "Utente non trovato.";
        }

        Utente utente = optionalUtente.get();

        if (utente.getReparto() == null) {
           return "L'utente non è assegnato a nessun reparto.";
        }

        if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
            return "La data di inizio deve essere precedente alla data di fine.";
        }

        Ferie ferie = new Ferie();
        ferie.setUtente(utente);
        ferie.setStartDate(startDate);
        ferie.setEndDate(endDate);
        ferie.setApproved(false);

        ferieRepository.save(ferie);

        return "Richiesta ferie salvata correttamente.";
    }




    @Transactional
    public String approveHolidaysForReparto(Long ferieId) {
        Optional<Ferie> ferieOpt = ferieRepository.findById(ferieId);
        if (ferieOpt.isEmpty()) {
            return "Ferie non trovate.";
        }

        Ferie ferie = ferieOpt.get();


        Utente capoReparto = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capo reparto non trovato"));

        if (capoReparto == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }

        if (!ferie.getUtente().getReparto().equals(capoReparto.getReparto())) {
            return "Non puoi approvare le ferie per un utente di un altro reparto.";
        }

        ferie.setApproved(false);

        ferieRepository.save(ferie);

        notificheService.notifyFerieAssegnate(
                ferie.getUtente(), ferie.getStartDate(), ferie.getEndDate(), capoReparto
        );

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

    @Transactional
    public void generateStockReport(Magazzino magazine) {
        Utente utente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (utente == null) {
            throw new IllegalArgumentException("Errore: Utente non trovato!");
        }

        Animale animale = animaleRepository.findFirstByOrderByIdAsc();
        if (animale == null) {
            System.err.println("Errore: Nessun paziente animale trovato nel sistema! Il report non verrà salvato.");
            return;
        }

        String reportMessage = "Report Magazzino\n" +
                "Stock Disponibile: " + magazine.getCurrentStock() + "\n" +
                "Capienza Massima: " + magazine.getMaximumCapacity();

        Report report = new Report();
        report.setStartDate(LocalDate.now());
        report.setEndDate(LocalDate.now().plusDays(7));
        report.setContent(reportMessage);

        reportRepository.save(report);

        Utente adminUser = utenteRepository.findByUsername("admin");
        if (adminUser != null) {
            notificheService.notifyAdmin(adminUser, reportMessage);
            System.out.println("Report inviato all'admin:\n" + reportMessage);
        } else {
            System.err.println(" Errore: Utente admin non trovato! Notifica non inviata.");
        }
    }

    @Transactional
    public void assegnaFerie(Long utenteId, LocalDate startDate, LocalDate endDate) {
        if (startDate.equals(endDate)) {
            throw new IllegalArgumentException("Le ferie devono coprire almeno due giorni.");
        }

        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        Utente capoReparto = utenteRepository.findByUsername(authenticationService.getUsername());
        if (capoReparto == null) {
            throw new IllegalArgumentException("Capo reparto non autenticato");
        }

        Ferie ferie = new Ferie();
        ferie.setUtente(utente);
        ferie.setStartDate(startDate);
        ferie.setEndDate(endDate);
        ferie.setApproved(false);


        ferieRepository.save(ferie);
        notificheService.notifyFerieAssegnate(utente, startDate, endDate, capoReparto);
    }


}
