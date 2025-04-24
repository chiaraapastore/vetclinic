package com.vetclinic.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtenteDTO {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String keycloakId;
    private String phoneNumber;
    private String profileImage;
    private String registrationNumber;
    private String role;
    private Long repartoId;
    private String nameDepartment;

    public UtenteDTO(Utente utente) {
        this.id = utente.getId();
        this.username = utente.getUsername();
        this.firstName = utente.getFirstName();
        this.lastName = utente.getLastName();
        this.email = utente.getEmail();
        this.keycloakId = utente.getKeycloakId();
        this.phoneNumber = utente.getPhoneNumber();
        this.profileImage = utente.getProfileImage();
        this.registrationNumber = utente.getRegistrationNumber();
        this.role = utente.getRole();

        if (utente.getReparto() != null) {
            this.repartoId = utente.getReparto().getId();
            this.nameDepartment = utente.getReparto().getName();
        } else {
            this.repartoId = null;
            this.nameDepartment = "Nessun reparto assegnato";
        }
    }
}
