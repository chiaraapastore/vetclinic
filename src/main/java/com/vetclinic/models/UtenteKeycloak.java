package com.vetclinic.models;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.keycloak.representations.idm.UserRepresentation;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class UtenteKeycloak extends UserRepresentation {
    private String username;
    private String email;
}



