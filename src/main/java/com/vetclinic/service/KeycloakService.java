package com.vetclinic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vetclinic.client.KeycloakClient;
import com.vetclinic.models.RoleKeycloak;
import com.vetclinic.models.TokenRequest;
import com.vetclinic.models.Utente;
import com.vetclinic.models.UtenteKeycloak;
import com.vetclinic.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final KeycloakClient keycloakClient;
    private final UtenteRepository utenteRepository;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    @Value("${keycloak.admin.realm}")
    private String realm;

    public String authenticate(String username, String password) {
        TokenRequest tokenRequest = new TokenRequest(username, password, clientId, clientSecret, "password");
        ResponseEntity<Object> responseEntity = keycloakClient.getAccessToken(tokenRequest);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> mapResponse = objectMapper.convertValue(responseEntity.getBody(), Map.class);
            return mapResponse.get("access_token").toString();
        } else {
            throw new RuntimeException("Login fallito: " + responseEntity.getStatusCode());
        }
    }

    public ResponseEntity<Utente> createUser(Utente utente) {
        if (utenteRepository.findByUsername(utente.getUsername()) != null) {
            throw new RuntimeException("Utente con username '" + utente.getUsername() + "' già esistente.");
        }

        String accessToken = authenticate(adminUsername, adminPassword);
        String authorizationHeader = "Bearer " + accessToken;

        UtenteKeycloak utenteKeycloak = convertToKeycloakUser(utente);
        ResponseEntity<Object> response = keycloakClient.createUsers(authorizationHeader, utenteKeycloak);

        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Errore nella creazione dell'utente su Keycloak.");
        }

        String keycloakId = getKeycloakUserId(response);
        utente.setKeycloakId(keycloakId);

        assignRole(authorizationHeader, keycloakId, utente.getRole());

        Utente savedUtente = utenteRepository.save(utente);

        return ResponseEntity.ok(savedUtente);
    }


    private String getKeycloakUserId(ResponseEntity<Object> response) {
        List<String> locations = response.getHeaders().get("location");
        if (locations == null || locations.isEmpty()) {
            throw new RuntimeException("Keycloak non ha restituito un ID utente.");
        }
        String[] parts = locations.get(0).split("/");
        return parts[parts.length - 1];
    }

    private void assignRole(String authorizationHeader, String userId, String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new RuntimeException("Il ruolo dell'utente è nullo o vuoto.");
        }


        ResponseEntity<List<RoleKeycloak>> rolesResponse = keycloakClient.getRealmRoles(authorizationHeader);
        if (rolesResponse == null || !rolesResponse.getStatusCode().is2xxSuccessful() || rolesResponse.getBody() == null) {
            throw new RuntimeException("Errore nel recupero dei ruoli disponibili in Keycloak.");
        }


        RoleKeycloak roleToAssign = rolesResponse.getBody().stream()
                .filter(role -> roleName.equalsIgnoreCase(role.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Ruolo '" + roleName + "' non trovato nei ruoli disponibili."));


        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName(roleToAssign.getName());
        roleRepresentation.setId(roleToAssign.getId());
        roleRepresentation.setComposite(false);

        ResponseEntity<Object> roleResponse = keycloakClient.addRealmRoleToUser(authorizationHeader, userId, List.of(roleRepresentation));

        if (roleResponse == null || !roleResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Errore nell'assegnazione del ruolo: " +
                    (roleResponse != null ? roleResponse.getBody() : "Nessuna risposta dal server."));
        }

        System.out.println("Ruolo '" + roleName + "' assegnato correttamente all'utente " + userId);
    }




    private UtenteKeycloak convertToKeycloakUser(Utente utente) {
        UtenteKeycloak keycloak = new UtenteKeycloak();
        keycloak.setUsername(utente.getUsername());
        keycloak.setFirstName(utente.getFirstName());
        keycloak.setLastName(utente.getLastName());
        keycloak.setEmail(utente.getEmail());

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setValue(utente.getPassword());
        credentialRepresentation.setTemporary(false);

        keycloak.setCredentials(List.of(credentialRepresentation));
        keycloak.setEnabled(true);
        return keycloak;
    }
}
