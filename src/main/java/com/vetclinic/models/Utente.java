package com.vetclinic.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Setter
@Getter
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


    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;




}