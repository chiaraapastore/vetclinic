package com.vetclinic.controller;

import com.vetclinic.models.Magazzino;
import com.vetclinic.service.MagazzinoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/stock")
public class MagazzinoController {

    private final MagazzinoService magazzinoService;

    public MagazzinoController(MagazzinoService magazzinoService) {
        this.magazzinoService = magazzinoService;
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

    @PutMapping("/update-stock")
    public ResponseEntity<Void> updateStock(@RequestBody Magazzino magazzino) {
        try {
            magazzinoService.updateStock(magazzino);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
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
