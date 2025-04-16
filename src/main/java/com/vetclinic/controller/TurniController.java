package com.vetclinic.controller;

import com.vetclinic.models.Turni;
import com.vetclinic.service.TurniService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/turni")
public class TurniController {

    private final TurniService turniService;

    public TurniController(TurniService turniService) {
        this.turniService = turniService;
    }

    @PostMapping("/assign/{dottoreId}")
    public ResponseEntity<Turni> assignShift(@PathVariable Long dottoreId,
                                             @RequestParam LocalDate startDate,
                                             @RequestParam LocalDate endDate) {
        Turni turno = turniService.assignTurno(dottoreId, startDate, endDate);
        return ResponseEntity.ok(turno);
    }

    @GetMapping("/{utenteId}")
    public ResponseEntity<List<Turni>> getShiftsForUser(@PathVariable Long utenteId) {
        List<Turni> shifts = turniService.getShiftsForUser(utenteId);
        return ResponseEntity.ok(shifts);
    }

    @GetMapping("/approved")
    public ResponseEntity<List<Turni>> getApprovedShifts() {
        List<Turni> approvedShifts = turniService.getShiftsApproved();
        return ResponseEntity.ok(approvedShifts);
    }

    @GetMapping("/not-approved")
    public ResponseEntity<List<Turni>> getNotApprovedShifts() {
        List<Turni> notApprovedShifts = turniService.getShiftsNotApproved();
        return ResponseEntity.ok(notApprovedShifts);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addShift(@RequestParam Long utenteId,
                                           @RequestParam LocalDate startTime,
                                           @RequestParam LocalDate endTime) {
        String response = turniService.addShift(utenteId, startTime, endTime);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/approve/{turnoId}")
    public ResponseEntity<String> approveShift(@PathVariable Long turnoId) {
        String response = turniService.approveShift(turnoId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/refuse/{turnoId}")
    public ResponseEntity<String> refuseShift(@PathVariable Long turnoId) {
        String response = turniService.refuseShift(turnoId);
        return ResponseEntity.ok(response);
    }
}
