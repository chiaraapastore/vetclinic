package com.vetclinic.service;

import com.vetclinic.models.Animale;
import com.vetclinic.models.Cliente;
import com.vetclinic.models.Fattura;
import com.vetclinic.models.Utente;
import com.vetclinic.repository.AnimaleRepository;
import com.vetclinic.repository.ClienteRepository;
import com.vetclinic.config.AuthenticationService;
import com.vetclinic.repository.FatturaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final AuthenticationService authenticationService;
    private final FatturaRepository fatturaRepository;
    private final AnimaleRepository animaleRepository;

    public ClienteService(ClienteRepository clienteRepository, AnimaleRepository animaleRepository,AuthenticationService authenticationService, FatturaRepository flatturaRepository) {
        this.clienteRepository = clienteRepository;
        this.authenticationService = authenticationService;
        this.fatturaRepository = flatturaRepository;
        this.animaleRepository = animaleRepository;
    }


    @Transactional
    public Cliente getClienteAutenticato() {
        return clienteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente non trovato"));
    }


    @Transactional
    public Cliente getClienteById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato"));
    }

    @Transactional
    public Cliente updateCliente(Long id, String firstName, String lastName, String phoneNumber, String email, String address) {
        Cliente cliente = getClienteById(id);
        cliente.setFirstName(firstName);
        cliente.setLastName(lastName);
        cliente.setPhoneNumber(phoneNumber);
        cliente.setEmail(email);

        return clienteRepository.save(cliente);
    }

    @Transactional
    public void deleteCliente(Long id) {
        Cliente cliente = getClienteById(id);
        clienteRepository.delete(cliente);
    }

    @Transactional
    public List<Animale> getAnimalsOfClient() {
        Cliente cliente = clienteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente non trovato"));

        return animaleRepository.findByClienteId(cliente.getId());
    }





    @Transactional
    public List<Fattura> getInvoicesOfClient() {
        Utente cliente = clienteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente non trovato"));

        return fatturaRepository.findByCliente(cliente);
    }


    private Animale animaleSetFind(Set<Animale> animaleSet, Long animaleId) {
        return animaleSet.stream()
                .filter(animale -> animale.getId().equals(animaleId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Animale non trovato o non associato al cliente"));
    }

}
