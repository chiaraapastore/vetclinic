package com.vetclinic.service;


import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.vetclinic.config.AuthenticationService;
import com.vetclinic.exception.UtenteNotFoundException;
import com.vetclinic.models.Utente;
import com.vetclinic.models.UtenteDTO;
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


    public Utente getUserDetailsDataBase(Long utenteId) {
        Utente utente = utenteRepository.findUtenteById(utenteId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con Keycloak ID: " + utenteId));
        System.out.println("Utente recuperato: " + utente);
        return utente;
    }


    private static final Logger log = LoggerFactory.getLogger(Utente.class);


    @Transactional
    public Utente getUtenteByKeycloakId(String keycloakId) {
        Utente utente = utenteRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con Keycloak ID: " + keycloakId));


        System.out.println("Utente recuperato: " + utente);
        return utente;
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

    public UtenteDTO getUserInfoByKeycloakId(String keycloakId) throws UtenteNotFoundException {
        Utente utente = utenteRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UtenteNotFoundException("Utente non trovato"));

        UtenteDTO utenteDTO = new UtenteDTO(utente);

        if (utente.getReparto() != null) {
            utenteDTO.setRepartoId(utente.getReparto().getId());
            utenteDTO.setNameDepartment(utente.getReparto().getName());
        } else {
            utenteDTO.setRepartoId(null);
            utenteDTO.setNameDepartment("Nessun reparto assegnato");
        }

        return utenteDTO;
    }
}

