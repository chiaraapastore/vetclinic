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
    public ResponseEntity<?> process(@PathVariable Long appointmentId) {
        Pagamento pagamento = pagamentoService.processPayment(appointmentId);
        return ResponseEntity.ok(pagamento);
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
