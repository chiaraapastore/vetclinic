package com.vetclinic.controller;

import com.vetclinic.service.MedicineService;
import com.vetclinic.models.Medicine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<Medicine> getMedicineById(@PathVariable Long id) {
        Optional<Medicine> medicine = medicineService.getMedicineById(id);
        return medicine.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/save")
    public ResponseEntity<Medicine> saveMedicine(@RequestBody Medicine medicine) {
        Medicine savedMedicine = medicineService.saveMedicine(medicine);
        return ResponseEntity.ok(savedMedicine);
    }

    @PutMapping("/{id}/update-available-quantity")
    public ResponseEntity<Medicine> updateMedicineAvailableQuantity(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {

        int availableQuantity = request.get("availableQuantity");

        medicineService.updateMedicineAvailableQuantity(id, availableQuantity);

        Medicine updated = medicineService.getMedicineById(id).orElseThrow(() -> new RuntimeException("Medicine not found after update!"));

        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medicine> updateMedicine(@PathVariable Long id, @RequestBody Medicine medicine) {
        Optional<Medicine> existingMedicine = medicineService.getMedicineById(id);
        if (existingMedicine.isPresent()) {
            Medicine updated = existingMedicine.get();
            updated.setQuantity(medicine.getQuantity());
            updated.setExpirationDate(medicine.getExpirationDate());
            medicineService.saveMedicine(updated);
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<Medicine>> getAvailableMedicines() {
        List<Medicine> medicines = medicineService.getAvailableMedicines();
        return ResponseEntity.ok(medicines);
    }
}
