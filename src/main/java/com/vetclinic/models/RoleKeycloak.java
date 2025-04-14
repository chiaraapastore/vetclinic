package com.vetclinic.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.keycloak.representations.idm.RoleRepresentation;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class RoleKeycloak {
    private String id;
    private String role;
    private String client;
    private String clientId;
    private String description;
    @Setter
    private String name;

    public RoleRepresentation toRoleRepresentation(){
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setId(this.id);
        roleRepresentation.setName(this.role);
        roleRepresentation.setDescription(this.description);
        return roleRepresentation;
    }

}
