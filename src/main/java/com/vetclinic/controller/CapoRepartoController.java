package com.vetclinic.controller;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.AnimaleRepository;
import com.vetclinic.repository.FerieRepository;
import com.vetclinic.repository.ReportRepository;
import com.vetclinic.repository.UtenteRepository;
import com.vetclinic.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/capo-reparto")
public class CapoRepartoController {

    private final CapoRepartoService capoRepartoService;
    private final AssistenteService assistenteService;
    private final VeterinarianService veterinarianService;
    private final AuthenticationService authenticationService;
    private final UtenteRepository utenteRepository;
    private final AnimaleRepository animaleRepository;
    private final TurniService turniService;
    private final FerieRepository ferieRepository;
    private final FerieService ferieService;
    private final MagazzinoService magazzinoService;
    private final ReportService reportService;



    public CapoRepartoController(CapoRepartoService capoRepartoService, ReportService reportService,MagazzinoService magazzinoService,FerieService ferieService,FerieRepository ferieRepository,TurniService turniService ,AnimaleRepository animaleRepository,UtenteRepository utenteRepository ,AuthenticationService authenticationService,AssistenteService assistenteService, VeterinarianService veterinarianService) {
        this.capoRepartoService = capoRepartoService;
        this.assistenteService = assistenteService;
        this.veterinarianService = veterinarianService;
        this.authenticationService = authenticationService;
        this.utenteRepository = utenteRepository;
        this.animaleRepository = animaleRepository;
        this.turniService = turniService;
        this.ferieRepository = ferieRepository;
        this.ferieService = ferieService;
        this.magazzinoService = magazzinoService;
        this.reportService = reportService;
    }


    @GetMapping("/reparti")
    public ResponseEntity<List<Reparto>> getReparti() {
        List<Reparto> reparti = capoRepartoService.getDepartments();
        return ResponseEntity.ok(reparti);
    }



    @PutMapping("/magazine/update-stock-and-report")
    public ResponseEntity<Void> updateStockAndSendReport(
            @RequestParam Long id,
            @RequestParam int currentStock,
            @RequestParam int maximumCapacity) {

        Magazzino magazine = new Magazzino();
        magazine.setId(id);
        magazine.setCurrentStock(currentStock);
        magazine.setMaximumCapacity(maximumCapacity);

        try {
            magazzinoService.updateStock(magazine);
            reportService.generateStockReport(magazine);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Errore nel backend: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }




    @PutMapping("/assegna-ferie/{utenteId}")
    public ResponseEntity<String> assegnaFerie(@PathVariable Long utenteId,
                                               @RequestParam("startDate") LocalDate startDate,
                                               @RequestParam("endDate") LocalDate endDate) {
        capoRepartoService.assegnaFerie(utenteId, startDate, endDate);
        return ResponseEntity.ok("Ferie assegnate");
    }



    @PostMapping("/ferie")
    public ResponseEntity<Map<String, String>> assignHolidays(
            @RequestParam Long dottoreId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        String result = capoRepartoService.addHolidaysForReparto(dottoreId, startDate, endDate);
        return ResponseEntity.ok(Map.of("message", result));
    }


    @PostMapping("/aggiungi-medicinale")
    public ResponseEntity<String> addMedicinal(@RequestBody Medicine medicinale) {
        String response = capoRepartoService.addMedicinal(medicinale);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/richiedi-ferie")
    public ResponseEntity<Map<String, String>> requestHolidays(@RequestParam Long utenteId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        String result = capoRepartoService.addHolidaysForReparto(utenteId, startDate, endDate);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PutMapping("/approva-ferie/{ferieId}")
    public ResponseEntity<Map<String, String>> approveHolidays(@PathVariable Long ferieId) {
        String result = capoRepartoService.approveHolidaysForReparto(ferieId);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @GetMapping("/ferie-approvate")
    public ResponseEntity<List<String>> getApprovedHolidayDays(@RequestParam Long repartoId) {
        List<Ferie> ferie = capoRepartoService.getHolidaysForReparto(repartoId);
        List<String> giorni = ferie.stream()
                .map(f -> f.getStartDate().getDayOfWeek().toString())
                .toList();
        return ResponseEntity.ok(giorni);
    }


    @GetMapping("/ferie-non-approvate")
    public ResponseEntity<Map<String, Object>> getUnapprovedHolidays(@RequestParam Long repartoId) {
        return ResponseEntity.ok(Map.of("ferie", capoRepartoService.getUnapprovedHolidaysForReparto(repartoId)));
    }

    @GetMapping("/ferie-non-approvate-capo")
    public ResponseEntity<?> getFerieNonApprovate(@RequestParam Long repartoId) {
        List<Ferie> ferie = ferieService.getNonApprovateByReparto(repartoId);
        ferie.forEach(f -> System.out.println(f.getUtente().getFirstName()));
        return ResponseEntity.ok(Map.of("ferie", ferie.stream().map(FerieDTO::new).toList()));
    }




    @GetMapping("/personale-reparto/{repartoId}")
    public ResponseEntity<Map<String, Object>> getPersonaleReparto(@PathVariable Long repartoId) {
        List<Veterinario> veterinari = veterinarianService.findByRepartoId(repartoId);
        List<Assistente> assistenti = assistenteService.findByRepartoId(repartoId);
        return ResponseEntity.ok(Map.of(
                "veterinari", veterinari,
                "assistenti", assistenti
        ));
    }

    @GetMapping("/animali-reparto")
    public ResponseEntity<List<Animale>> getAnimaliDelReparto() {
        Utente capoReparto = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capo reparto non trovato"));

        if (capoReparto.getReparto() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il capo reparto non è associato a un reparto.");
        }

        Long repartoId = capoReparto.getReparto().getId();
        List<Animale> animali = animaleRepository.findByRepartoId(repartoId);

        return ResponseEntity.ok(animali);
    }



    @PostMapping("/assegna-turno")
    public ResponseEntity<Turni>  assegnaTurno(@RequestParam Long dottoreId,
                                               @RequestParam LocalDate startDate,
                                               @RequestParam LocalDate endDate) {
        Turni turno = turniService.assignTurno(dottoreId, startDate, endDate);
        return ResponseEntity.ok(turno);
    }

    @DeleteMapping("/rifiuta-ferie/{ferieId}")
    public ResponseEntity<Map<String, String>> rejectHolidays(@PathVariable Long ferieId) {
        Optional<Ferie> ferieOpt = ferieRepository.findById(ferieId);
        if (ferieOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Richiesta ferie non trovata."));
        }

        ferieRepository.deleteById(ferieId);
        return ResponseEntity.ok(Map.of("message", "Richiesta ferie rifiutata."));
    }




}
