package com.vetclinic.controller;

import com.vetclinic.models.Animale;
import com.vetclinic.models.Cliente;
import com.vetclinic.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/cliente")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/me")
    public ResponseEntity<Cliente> getCliente() {
        Cliente cliente = clienteService.getClienteByUsername();
        return ResponseEntity.ok(cliente);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Cliente> updateCliente(@PathVariable Long id,
                                                 @RequestParam String firstName,
                                                 @RequestParam String lastName,
                                                 @RequestParam String phoneNumber,
                                                 @RequestParam String email,
                                                 @RequestParam String address) {
        Cliente updatedCliente = clienteService.updateCliente(id, firstName, lastName, phoneNumber, email, address);
        return ResponseEntity.ok(updatedCliente);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteCliente(@PathVariable Long id) {
        clienteService.deleteCliente(id);
        return ResponseEntity.ok(Map.of("message", "Cliente eliminato con successo"));
    }

    @GetMapping("/animals")
    public ResponseEntity<Set<Animale>> getAnimalsOfClient() {
        Set<Animale> animali = clienteService.getAnimalsOfClient();
        return ResponseEntity.ok(animali);
    }
}
