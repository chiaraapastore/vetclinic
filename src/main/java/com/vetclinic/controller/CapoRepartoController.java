package com.vetclinic.controller;

import com.vetclinic.models.Medicine;
import com.vetclinic.models.Reparto;
import com.vetclinic.service.CapoRepartoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/capo-reparto")
public class CapoRepartoController {

    private final CapoRepartoService capoRepartoService;

    public CapoRepartoController(CapoRepartoService capoRepartoService) {
        this.capoRepartoService = capoRepartoService;
    }


    @GetMapping("/reparti")
    public ResponseEntity<List<Reparto>> getDepartments(){
        List<Reparto> reparti = capoRepartoService.getDepartments();
        return ResponseEntity.ok(reparti);
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
    public ResponseEntity<Map<String, Object>> getApprovedHolidays(@RequestParam Long repartoId) {
        return ResponseEntity.ok(Map.of("ferie", capoRepartoService.getHolidaysForReparto(repartoId)));
    }

    @GetMapping("/ferie-non-approvate")
    public ResponseEntity<Map<String, Object>> getUnapprovedHolidays(@RequestParam Long repartoId) {
        return ResponseEntity.ok(Map.of("ferie", capoRepartoService.getUnapprovedHolidaysForReparto(repartoId)));
    }

}
