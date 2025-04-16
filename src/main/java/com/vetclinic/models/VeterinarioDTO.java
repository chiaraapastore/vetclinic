package com.vetclinic.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
@DiscriminatorValue("veterinario")
public class VeterinarioDTO extends Utente {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String registrationNumber;
        @NotBlank(message = "La specializzazione è obbligatoria")
        private String specialization;
        private boolean available;
        private boolean isDepartmentHead;



        public VeterinarioDTO(Long id, String firstName, String lastName, String email, String registrationNumber, String departmentName) {
                this.id = id;
                this.firstName = firstName;
                this.lastName = lastName;
                this.email = email;
                this.registrationNumber = registrationNumber;
                this.specialization = departmentName;
                this.available = true;
                this.isDepartmentHead = false;
        }



}
