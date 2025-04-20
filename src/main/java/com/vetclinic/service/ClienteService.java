package com.vetclinic.service;

import com.vetclinic.models.Animale;
import com.vetclinic.models.Cliente;
import com.vetclinic.models.DocumentoClinico;
import com.vetclinic.models.Fattura;
import com.vetclinic.repository.ClienteRepository;
import com.vetclinic.config.AuthenticationService;
import com.vetclinic.repository.DocumentoClinicoRepository;
import com.vetclinic.repository.FatturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final AuthenticationService authenticationService;
    private final FatturaRepository fatturaRepository;
    private final DocumentoClinicoRepository documentoClinicoRepository;

    public ClienteService(ClienteRepository clienteRepository, AuthenticationService authenticationService, FatturaRepository flatturaRepository, DocumentoClinicoRepository documentoClinicoRepository) {
        this.clienteRepository = clienteRepository;
        this.authenticationService = authenticationService;
        this.fatturaRepository = flatturaRepository;
        this.documentoClinicoRepository = documentoClinicoRepository;
    }

    @Transactional
    public Cliente getClienteByUsername() {
        String username = authenticationService.getUsername();
        Optional<Cliente> cliente = clienteRepository.findByUsername(username);

        return cliente.orElseThrow(() -> new IllegalArgumentException("Cliente non trovato"));
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
        cliente.setAddress(address);

        return clienteRepository.save(cliente);
    }

    @Transactional
    public void deleteCliente(Long id) {
        Cliente cliente = getClienteById(id);
        clienteRepository.delete(cliente);
    }

    @Transactional
    public Set<Animale> getAnimalsOfClient() {
        String username = authenticationService.getUsername();
        Cliente cliente = clienteRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato"));
        return cliente.getAnimaleSet();
    }

    @Transactional
    public List<DocumentoClinico> getDocumentsOfClientAnimal(Long animaleId) {
        String username = authenticationService.getUsername();
        Cliente cliente = clienteRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato"));

        Animale animale = animaleSetFind(cliente.getAnimaleSet(), animaleId);


        return documentoClinicoRepository.findByAnimaleId(animale.getId());
    }

    @Transactional
    public List<Fattura> getInvoicesOfClient() {
        String username = authenticationService.getUsername();
        Cliente cliente = clienteRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato"));

        return fatturaRepository.findByCliente(cliente);
    }

    private Animale animaleSetFind(Set<Animale> animaleSet, Long animaleId) {
        return animaleSet.stream()
                .filter(animale -> animale.getId().equals(animaleId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Animale non trovato o non associato al cliente"));
    }

}
