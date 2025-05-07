package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final AuthenticationService authenticationService;
    private final UtenteRepository utenteRepository;
    private final MedicineRepository medicineRepository;
    private final NotificheService notificheService;
    private final AnimaleRepository animaleRepository;

    public ReportService(ReportRepository reportRepository, AnimaleRepository animaleRepository,AuthenticationService authenticationService, UtenteRepository utenteRepository, MedicineRepository medicineRepository, NotificheService notificheService) {
        this.reportRepository = reportRepository;
        this.authenticationService = authenticationService;
        this.utenteRepository = utenteRepository;
        this.medicineRepository = medicineRepository;
        this.notificheService = notificheService;
        this.animaleRepository = animaleRepository;
    }



    @Transactional
    public List<Map<String, Object>> getReportConsumi() {
        Utente utente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (utente == null || !utente.getRole().equals("admin")) {
            throw new IllegalArgumentException("Utente non autenticato o non autorizzato");
        }

        return medicineRepository.findConsumoPerReparto();
    }

    @Transactional
    public Report createReport(Report report) {
        Utente utente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (utente == null) {
            throw new IllegalArgumentException("Utente non trovato");
        }

        if (report.getStartDate() == null) {
            report.setStartDate(LocalDate.now());
        }
        if (report.getEndDate() == null) {
            report.setEndDate(LocalDate.now().plusDays(7));
        }

        Report savedReport = reportRepository.save(report);


        notificheService.createAndSendNotification(utente, utente, "Il tuo report è stato creato con successo.", "report_creation");

        return savedReport;
    }

    @Transactional
    public List<Report> getAllReports() {
        Utente utente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (utente == null) {
            throw new IllegalArgumentException("Utente non trovato");
        }
        return reportRepository.findAll();
    }

    @Transactional
    public Optional<Report> getReportById(String id) {
        Utente utente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (utente == null) {
            throw new IllegalArgumentException("Utente non trovato");
        }
        return reportRepository.findById(id);
    }

    @Transactional
    public Report updateReport(String id, Report updatedReport) {
        Utente utente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (utente == null) {
            throw new IllegalArgumentException("Utente non trovato");
        }

        return reportRepository.findById(id).map(existingReport -> {
            existingReport.setStartDate(updatedReport.getStartDate());
            existingReport.setEndDate(updatedReport.getEndDate());
            Report savedReport = reportRepository.save(existingReport);

            notificheService.createAndSendNotification(utente, utente, "Il tuo report è stato aggiornato con successo.", "report_update");

            return savedReport;
        }).orElseThrow(() -> new RuntimeException("Report non trovato"));
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

}
