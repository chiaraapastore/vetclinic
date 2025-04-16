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
    private final EmergenzaRepository emergenzaRepository;
    private final MedicineRepository medicineRepository;
    private final NotificheService notificheService;

    public ReportService(ReportRepository reportRepository, AuthenticationService authenticationService, UtenteRepository utenteRepository, EmergenzaRepository emergenzaRepository, MedicineRepository medicineRepository, NotificheService notificheService) {
        this.reportRepository = reportRepository;
        this.authenticationService = authenticationService;
        this.utenteRepository = utenteRepository;
        this.emergenzaRepository = emergenzaRepository;
        this.medicineRepository = medicineRepository;
        this.notificheService = notificheService;
    }

    @Transactional
    public List<Map<String, Object>> getEmergencyReport() {

           Utente utente = utenteRepository.findByUsername(authenticationService.getUsername());
            if (utente == null || !utente.getRole().equals("admin")) {
                throw new IllegalArgumentException("Utente non autenticato o non autorizzato");
            }

            List<Map<String, Object>> emergencyReports = emergenzaRepository.findAll().stream()
                    .map(emergency -> {
                        Map<String, Object> reportMap = new HashMap<>();
                        reportMap.put("emergencyId", emergency.getId());
                        reportMap.put("animalName", emergency.getAnimal().getName());
                        reportMap.put("veterinarianName", emergency.getVeterinarian().getFirstName() + " " + emergency.getVeterinarian().getLastName());
                        reportMap.put("emergencyDate", emergency.getEmergencyDate());
                        reportMap.put("medicine", emergency.getMedicine().getName());
                        reportMap.put("dosage", emergency.getDosage());
                        reportMap.put("description", emergency.getDescription());
                        return reportMap;
                    })
                    .collect(Collectors.toList());

            notificheService.createAndSendNotification(utente, utente, "Nuovo report di emergenza generato", "emergency_report");

            return emergencyReports;
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
}
