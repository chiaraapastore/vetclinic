package com.vetclinic.service;

import com.vetclinic.models.Animale;
import com.vetclinic.models.Cliente;
import com.vetclinic.repository.ClienteRepository;
import com.vetclinic.config.AuthenticationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final AuthenticationService authenticationService;

    public ClienteService(ClienteRepository clienteRepository, AuthenticationService authenticationService) {
        this.clienteRepository = clienteRepository;
        this.authenticationService = authenticationService;
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
}
