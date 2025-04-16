package com.vetclinic.service;


import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.Utente;
import com.vetclinic.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Service
public class UtenteService {

    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;
    private static final String UPLOAD_DIR = "uploads";

    public UtenteService(UtenteRepository utenteRepository, AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
        this.utenteRepository = utenteRepository;
    }


    @Transactional
    public Utente getUtenteByEmail(String email) {
        Utente utente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (utente == null) {
            throw new IllegalArgumentException("Utente non trovato");
        }
        return utenteRepository.findByEmail(email);
    }


    @Transactional
    public Utente updateUtente(Long id, Utente utenteDetails) {
        Utente authenticatedUtente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (authenticatedUtente == null) {
            throw new IllegalArgumentException("Utente autenticato non trovato");
        }
        Utente utente = utenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        utente.setFirstName(utenteDetails.getFirstName());
        utente.setLastName(utenteDetails.getLastName());

        return utenteRepository.save(utente);
    }

    @Transactional
    public boolean deleteUtente(String username) {
        Utente userToDelete = utenteRepository.findByUsername(username);
        if (userToDelete != null) {
            utenteRepository.delete(userToDelete);
            return true;
        }
        return false;
    }


    @Transactional
    public boolean userExistsByUsername(String username) {
        return utenteRepository.findByUsername(username) != null;
    }

    @Transactional
    public Utente getUserDetailsDataBase() {
        String username = authenticationService.getUsername();
        return utenteRepository.findByUsername(username);
    }

    @Transactional
    public Utente getUtenteByKeycloakId(String keycloakId) {
        return utenteRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("Nessun utente trovato con Keycloak ID: " + keycloakId));
    }


    @Transactional
    public String uploadProfileImage(Long id, MultipartFile file) throws IOException {
        Utente utente = utenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + id));

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = "profile_" + id + "_" + System.currentTimeMillis() + ".jpg";
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        utente.setProfileImage("/" + UPLOAD_DIR + fileName);
        utenteRepository.save(utente);

        return utente.getProfileImage();
    }
}

