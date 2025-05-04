package com.vetclinic.service;


import com.vetclinic.models.Fornitore;
import com.vetclinic.models.Ordine;
import com.vetclinic.repository.OrdineRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrdineService {

    private final OrdineRepository ordineRepository;

    public OrdineService(OrdineRepository ordineRepository) {
        this.ordineRepository = ordineRepository;
    }

    @Transactional
    public Ordine createOrder(Fornitore fornitore, int quantity) {
        Ordine ordine = new Ordine();
        ordine.setSupplier(fornitore);
        ordine.setQuantity(quantity);
        ordine.setOrderDate(new java.util.Date());
        ordine.setStatus(Ordine.OrderStatus.PENDING);
        return ordineRepository.save(ordine);
    }


    @Transactional
    public List<Ordine> getOrders() {
        return ordineRepository.findAll();
    }


    @Transactional
    public List<Ordine> getPendingOrders() {
        return ordineRepository.findByStatus(Ordine.OrderStatus.PENDING);
    }

    @Transactional
    public List<Ordine> getOrderHistory() {
        return ordineRepository.findAll();
    }

    @Transactional
    public Ordine updateOrderStatus(Long ordineId, Ordine.OrderStatus nuovoStato) {
        Ordine ordine = ordineRepository.findById(ordineId)
                .orElseThrow(() -> new IllegalArgumentException("Ordine non trovato"));
        ordine.setStatus(nuovoStato);
        return ordineRepository.save(ordine);
    }

    @Transactional
    public Ordine aggiornaStatoOrdine(Long ordineId, Ordine.OrderStatus nuovoStato) {
        Ordine ordine = ordineRepository.findById(ordineId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordine non trovato con ID: " + ordineId));

        ordine.setStatus(nuovoStato);
        return ordineRepository.save(ordine);
    }

}
