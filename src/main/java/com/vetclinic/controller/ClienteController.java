package com.vetclinic.controller;

import java.util.List;
import com.vetclinic.models.Animale;
import com.vetclinic.models.Cliente;
import com.vetclinic.models.Fattura;
import com.vetclinic.service.AnimaleService;
import com.vetclinic.service.ClienteService;
import com.vetclinic.service.FatturaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/cliente")
public class ClienteController {

    private final ClienteService clienteService;
    private final AnimaleService animaleService;
    private final FatturaService fatturaService;

    public ClienteController(ClienteService clienteService, AnimaleService animaleService, FatturaService fatturaService) {
        this.clienteService = clienteService;
        this.animaleService = animaleService;
        this.fatturaService = fatturaService;
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
    public ResponseEntity<List<Animale>> getAnimalsOfClient() {
        List<Animale> animali = clienteService.getAnimalsOfClient();
        return ResponseEntity.ok(animali);
    }


    @GetMapping("/animals/{animalId}/download-pdf")
    public ResponseEntity<byte[]> downloadAnimalDocument(@PathVariable Long animalId) throws Exception {
        byte[] pdf = animaleService.generateDocumentoClinicoAndFatturaPdf(animalId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "documento_clinico_animale_" + animalId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }


    @GetMapping("/fatture")
    public ResponseEntity<List<Fattura>> getMyInvoices() {
        List<Fattura> fatture = clienteService.getInvoicesOfClient();
        return ResponseEntity.ok(fatture);
    }
}
