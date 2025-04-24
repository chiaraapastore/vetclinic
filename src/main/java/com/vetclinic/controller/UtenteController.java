package com.vetclinic.controller;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.exception.UtenteNotFoundException;
import com.vetclinic.models.TokenRequest;
import com.vetclinic.models.Utente;
import com.vetclinic.models.UtenteDTO;
import com.vetclinic.repository.UtenteRepository;
import com.vetclinic.service.KeycloakService;
import com.vetclinic.service.UtenteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/utente")
public class UtenteController {

    private final UtenteService utenteService;
    private final KeycloakService keycloakService;
    private final AuthenticationService authenticationService;
    private final UtenteRepository utenteRepository;

    public UtenteController(UtenteService utenteService, KeycloakService keycloakService, AuthenticationService authenticationService, UtenteRepository utenteRepository) {
        this.utenteService = utenteService;
        this.keycloakService = keycloakService;
        this.authenticationService = authenticationService;
        this.utenteRepository = utenteRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody TokenRequest tokenRequest) {
        try {
            String token = keycloakService.authenticate(tokenRequest.getUsername(), tokenRequest.getPassword());
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenziali non valide");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Object> createUser(@RequestBody Utente utente) {
        try {
            ResponseEntity<Utente> savedUtente = keycloakService.createUser(utente);
            return ResponseEntity.ok(savedUtente);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/user-info")
    public ResponseEntity<UtenteDTO> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String keycloakId = jwt.getClaim("sub");
        try {
            UtenteDTO utenteDTO = utenteService.getUserInfoByKeycloakId(keycloakId);
            return ResponseEntity.ok(utenteDTO);
        } catch (UtenteNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/utenti/{email}")
    public ResponseEntity<Utente> getUtenteByEmail(@PathVariable String email) {
        Utente utente = utenteService.getUtenteByEmail(email);
        if (utente == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(utente);
    }


    @PutMapping("/utenti/{id}")
    public ResponseEntity<Utente> updateUtente(@PathVariable Long id, @RequestBody Utente utenteDetails) {
        Utente updatedUtente = utenteService.updateUtente(id, utenteDetails);
        if (updatedUtente == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedUtente);
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<Void> deleteUtente(@PathVariable String username) {
        boolean isDeleted = utenteService.deleteUtente(username);
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }




    @GetMapping("/userDetailsDataBase/{utenteId}")
    public ResponseEntity<Utente> getUserDetailsDataBase(@PathVariable Long utenteId) {
        try{
            Utente utente= utenteService.getUserDetailsDataBase(utenteId);
            System.out.println("Ciao");
            System.out.println(utente);
            return new ResponseEntity<>(utente, HttpStatus.OK);
        }catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkUserExists(@RequestParam String username) {
        boolean exists = utenteService.userExistsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/upload-profile-image/{id}")
    public ResponseEntity<String> uploadProfileImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = utenteService.uploadProfileImage(id, file);
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore nel caricamento dell'immagine.");
        }
    }

}