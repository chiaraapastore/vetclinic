package com.vetclinic.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "appuntamento")
public class Appuntamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "animal_id")
    private Animale animal;

    @ManyToOne
    @JoinColumn(name = "veterinario_id")
    private VeterinarioDTO veterinarian;

    private Date appointmentDate;

    private String reason;

    private String status;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}

