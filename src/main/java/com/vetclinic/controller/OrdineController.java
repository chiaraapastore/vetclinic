package com.vetclinic.controller;


import com.vetclinic.models.Fornitore;
import com.vetclinic.models.Ordine;
import com.vetclinic.repository.FornitoreRepository;
import com.vetclinic.service.OrdineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/admin")
public class OrdineController {

    private final OrdineService ordineService;
    private final FornitoreRepository fornitoreRepository;

    public OrdineController(OrdineService ordineService, FornitoreRepository fornitoreRepository) {
        this.ordineService = ordineService;
        this.fornitoreRepository = fornitoreRepository;
    }

    @PostMapping("/ordini")
    public ResponseEntity<Ordine> creaOrdine(@RequestBody Map<String, Object> payload) {
        Long fornitoreId = Long.valueOf(payload.get("fornitoreId").toString());
        int quantity = Integer.parseInt(payload.get("quantity").toString());

        Optional<Fornitore> fornitoreOpt = fornitoreRepository.findById(fornitoreId);
        if (fornitoreOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        Fornitore fornitore = fornitoreOpt.get();
        Ordine nuovoOrdine = ordineService.createOrder(fornitore, quantity);

        return ResponseEntity.ok(nuovoOrdine);
    }

    @GetMapping("/ordini")
    public ResponseEntity<List<Ordine>> getOrders() {
        List<Ordine> ordini = ordineService.getOrders();
        return ResponseEntity.ok(ordini);
    }

    @PutMapping("/ordini/{ordineId}/stato")
    public ResponseEntity<Ordine> updateOrderStatus(@PathVariable Long ordineId, @RequestBody Map<String, String> payload) {
        Ordine.OrderStatus nuovoStato = Ordine.OrderStatus.valueOf(payload.get("stato"));
        Ordine ordineAggiornato = ordineService.updateOrderStatus(ordineId, nuovoStato);
        return ResponseEntity.ok(ordineAggiornato);
    }
}
