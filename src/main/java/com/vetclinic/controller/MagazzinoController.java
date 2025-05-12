package com.vetclinic.controller;

import com.vetclinic.models.Magazzino;
import com.vetclinic.service.MagazzinoService;
import com.vetclinic.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/stock")
public class MagazzinoController {

    private final MagazzinoService magazzinoService;
    private final ReportService reportService;


    public MagazzinoController(MagazzinoService magazzinoService, ReportService reportService) {
        this.magazzinoService = magazzinoService;
        this.reportService = reportService;
    }

    @GetMapping("/dettagli")
    public ResponseEntity<Magazzino> getStockDetails() {
        Magazzino magazzino = magazzinoService.getStock();
        return ResponseEntity.ok(magazzino);
    }

    @PostMapping("/create")
    public ResponseEntity<Magazzino> createStock(@RequestBody Magazzino magazzino) {
        return ResponseEntity.ok(magazzinoService.createStock(magazzino));
    }

    @GetMapping("/current-stock")
    public ResponseEntity<Magazzino> getCurrentStock() {
        try {
            return ResponseEntity.ok(magazzinoService.getStock());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/update-stock-and-report")
    public ResponseEntity<Void> updateStockAndSendReport(@RequestBody Magazzino magazine) {
        System.out.println("Dati ricevuti dal frontend: " + magazine);

        if (magazine == null || magazine.getId() == null) {
            System.err.println("Errore: Magazine nullo o senza ID!");
            return ResponseEntity.badRequest().build();
        }

        try {
            magazzinoService.updateStock(magazine);
            reportService.generateStockReport(magazine);
            System.out.println("Stock aggiornato e report inviato!");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Errore nel backend: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/update-stock/{medicineId}")
    public ResponseEntity<String> updateStock(@PathVariable Long medicineId, @RequestParam int quantity) {
        magazzinoService.checkAndUpdateMedicineStock(medicineId, quantity);
        return ResponseEntity.ok("Stock updated successfully");
    }

    @PutMapping("/update-medicine-stock")
    public ResponseEntity<String> updateMedicineStock(@RequestParam Long medicineId, @RequestParam int quantity) {
        try {
            boolean updated = magazzinoService.updateMedicineStock(medicineId, quantity);
            if (updated) {
                return ResponseEntity.ok("Scorte di medicinali aggiornate con successo");
            } else {
                return ResponseEntity.badRequest().body("Scorte insufficienti o medicinale non trovato");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
