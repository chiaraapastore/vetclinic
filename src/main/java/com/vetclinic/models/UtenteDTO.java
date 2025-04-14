package com.vetclinic.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UtenteDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String registrationNumber;
    private String phoneNumber;
    private String profileImage;
    private String role;
    private int countNotification;
    private String keycloakId;
    private Long repartoId;
    private String nameDepartment;


    public UtenteDTO(Utente utente) {
        this.id = utente.getId();
        this.username = utente.getUsername();
        this.firstName = utente.getFirstName();
        this.lastName = utente.getLastName();
        this.email = utente.getEmail();
        this.registrationNumber = utente.getRegistrationNumber();
        this.phoneNumber = utente.getPhoneNumber();
        this.profileImage = utente.getProfileImage();
        this.role = utente.getRole();
        this.countNotification = utente.getCountNotification();
        this.keycloakId = utente.getKeycloakId();
        if (utente.getReparto() != null) {
            this.repartoId = utente.getReparto().getId();
            this.nameDepartment = utente.getReparto().getName();
        } else {
            this.repartoId = null;
            this.nameDepartment= "Nessun reparto assegnato";
        }

    }

}
