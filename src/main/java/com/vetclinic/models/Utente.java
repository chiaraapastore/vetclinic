package com.vetclinic.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "utente")


public class Utente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username obbligatorio")
    private String username;
    @NotBlank(message = "Il nome è obbligatorio")
    private String firstName;
    @NotBlank(message = "Il cognome è obbligatorio")
    private String lastName;
    @NotBlank(message = "Email obbligatoria")
    private String email;
    private String registrationNumber;
    private String phoneNumber;
    private String password;

    @Column(name = "profile_image")
    private String profileImage;
    private String role;
    private int countNotification;
    private String keycloakId;

    @Setter
    @ManyToOne
    @JoinColumn(name = "reparto_id", referencedColumnName = "id")
    private Reparto reparto;

    @Transient
    public String getRepartoNome() {
        return reparto != null ? reparto.getName() : "Nessun reparto assegnato";
    }

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;




}