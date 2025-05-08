package com.vetclinic.controller;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.Ferie;
import com.vetclinic.service.FerieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/ferie")
public class FerieController {

    private final FerieService ferieService;
    private final AuthenticationService authenticationService;

    public FerieController(FerieService ferieService, AuthenticationService authenticationService) {
        this.ferieService = ferieService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/utente/{utenteId}")
    public ResponseEntity<List<Ferie>> getHolidaysForUser(@PathVariable Long utenteId) {
        List<Ferie> ferie = ferieService.getHolidaysForUser(utenteId);
        return ResponseEntity.ok(ferie);
    }

    @GetMapping("/approvate")
    public ResponseEntity<List<Ferie>> getHolidaysApproved() {
        List<Ferie> ferie = ferieService.getHolidaysApproved();
        return ResponseEntity.ok(ferie);
    }

    @PostMapping("/richiedi")
    public ResponseEntity<Map<String, String>> addHolidays(@RequestParam Long utenteId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        String result = ferieService.addHolidays(utenteId, startDate, endDate);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PutMapping("/approve/{ferieId}")
    public ResponseEntity<String> approveHolidays(@PathVariable Long ferieId) {
        String response = ferieService.approveHolidays(ferieId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/refuse/{ferieId}")
    public ResponseEntity<String> refuseHolidays(@PathVariable Long ferieId) {
        String response = ferieService.refuseHolidays(ferieId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/mie-ferie")
    public ResponseEntity<List<Ferie>> getMieFerie(@RequestParam LocalDate startDate,
                                                   @RequestParam LocalDate endDate) {
        Long utenteId = ferieService.getUserIdByKeycloak();
        List<Ferie> ferie = ferieService.getHolidaysForUserInRange(utenteId, startDate, endDate);
        return ResponseEntity.ok(ferie);
    }

    @GetMapping("/ferie-approvate-dettagliate")
    public ResponseEntity<List<Ferie>> getFerieApprovateDettagliate(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<Ferie> ferie = ferieService.getFerieApprovatePerUtente(start, end);
        return ResponseEntity.ok(ferie);
    }


}