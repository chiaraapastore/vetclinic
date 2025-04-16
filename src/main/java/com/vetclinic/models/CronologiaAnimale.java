package com.vetclinic.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cronologia_animale")
public class CronologiaAnimale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String noteVet;
    private String followUp;
    private String medicalHistory;
    private String symptoms;

    @ManyToOne
    @JoinColumn(name = "animal_id")
    private Animale animal;

    @ManyToOne
    @JoinColumn(name = "veterinario_id")
    private VeterinarioDTO veterinarian;

    @OneToMany(mappedBy = "animale")
    private List<Operazione> operazione;

    @OneToMany(mappedBy = "animale")
    private List<Esame> esame;

    @OneToMany(mappedBy = "animale")
    private List<Trattamento> trattamento;

    @OneToMany(mappedBy = "animale")
    private List<Vaccino> vaccini;


    @ManyToOne
    @JoinColumn(name = "assistente_id")
    private Assistente assistant;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    private Date eventDate;
    private String eventType;
    private String description;
}

