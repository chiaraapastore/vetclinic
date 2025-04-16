package com.vetclinic.controller;

import com.vetclinic.models.Pagamento;
import com.vetclinic.service.PagamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/pagamenti")
public class PagamentoController {

    private final PagamentoService pagamentoService;


    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping("/process/{appointmentId}")
    public ResponseEntity<Pagamento> processPayment(@PathVariable Long appointmentId, @RequestParam double amount) {
        try {
            Pagamento payment = pagamentoService.processPayment(appointmentId, amount);
            return ResponseEntity.ok(payment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/update-status/{paymentId}")
    public ResponseEntity<Pagamento> updatePaymentStatus(@PathVariable Long paymentId, @RequestParam String status) {
        try {
            Pagamento payment = pagamentoService.updatePaymentStatus(paymentId, status);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
