package com.vetclinic.service;


import com.vetclinic.models.Fornitore;
import com.vetclinic.repository.FornitoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FornitoreService {

    private final FornitoreRepository fornitoreRepository;

    @Autowired
    public FornitoreService(FornitoreRepository fornitoreRepository) {
        this.fornitoreRepository = fornitoreRepository;
    }
   public Fornitore addFornitore(Fornitore fornitore) {
        return fornitoreRepository.save(fornitore);
    }

    public List<Fornitore> getAllFornitori() {
        return fornitoreRepository.findAll();
    }

    public Optional<Fornitore> getFornitoreById(Long id) {
        return fornitoreRepository.findById(id);
    }

    public void updateFornitore(Long id, Fornitore fornitoreDetails) {
        Fornitore fornitore = fornitoreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fornitore non trovato con ID: " + id));

        fornitore.setFirstName(fornitoreDetails.getFirstName());
        fornitore.setLastName(fornitoreDetails.getLastName());
        fornitore.setAddress(fornitoreDetails.getAddress());

        fornitoreRepository.save(fornitore);
    }

    public void deleteFornitore(Long id) {
        Fornitore fornitore = fornitoreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fornitore non trovato con ID: " + id));
        fornitoreRepository.delete(fornitore);
    }
}
