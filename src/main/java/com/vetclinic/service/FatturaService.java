package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.FatturaRepository;
import com.vetclinic.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;

@Service
public class FatturaService {

    private final FatturaRepository fatturaRepository;
    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;

    public FatturaService(FatturaRepository fatturaRepository, UtenteRepository utenteRepository, AuthenticationService authenticationService) {
        this.fatturaRepository = fatturaRepository;
        this.utenteRepository = utenteRepository;
        this.authenticationService = authenticationService;
    }

    @Transactional
    public Fattura createInvoice(Long appointmentId, double amount) {
        Utente client = utenteRepository.findByUsername(authenticationService.getUsername());
        if (client == null) {
            throw new IllegalArgumentException("Cliente non trovato");
        }

        Fattura invoice = new Fattura();
        invoice.setCliente((Cliente) client);
        invoice.setIssueDate(new java.util.Date());
        invoice.setAmount(amount);
        invoice.setStatus("PENDING");

        return fatturaRepository.save(invoice);
    }

    @Transactional
    public Fattura getInvoiceById(Long id) {
        return fatturaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fattura non trovata"));
    }

    @Transactional
    public void updateInvoiceStatus(Long id, String status) {
        Fattura fattura = fatturaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fattura non trovata"));
        fattura.setStatus(status);
        fatturaRepository.save(fattura);
    }

    @Transactional
    public void deleteInvoice(Long id) {
        Fattura fattura = fatturaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fattura non trovata"));
        fatturaRepository.delete(fattura);
    }

}
