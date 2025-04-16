package com.vetclinic.controller;

import com.vetclinic.models.Fattura;
import com.vetclinic.service.FatturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fatture")
public class FatturaController {

    private final FatturaService fatturaService;

    public FatturaController(FatturaService fatturaService) {
        this.fatturaService = fatturaService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createInvoice(@RequestParam Long appointmentId, @RequestParam double amount) {
        Fattura fattura = fatturaService.createInvoice(appointmentId, amount);
        return ResponseEntity.ok(Map.of("message", "Fattura creata con successo", "fatturaId", String.valueOf(fattura.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fattura> getInvoice(@PathVariable Long id) {
        Fattura fattura = fatturaService.getInvoiceById(id);
        return ResponseEntity.ok(fattura);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, String>> updateInvoiceStatus(@PathVariable Long id, @RequestParam String status) {
        fatturaService.updateInvoiceStatus(id, status);
        return ResponseEntity.ok(Map.of("message", "Stato fattura aggiornato con successo"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteInvoice(@PathVariable Long id) {
        fatturaService.deleteInvoice(id);
        return ResponseEntity.ok(Map.of("message", "Fattura eliminata con successo"));
    }

}
