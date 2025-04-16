package com.vetclinic.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "paziente")

public class Animale {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotBlank(message = "Il nome è obbligatorio")
        private String name;

        @NotBlank(message = "La specie è obbligatoria")
        private String species;

        @NotBlank(message = "La razza è obbligatoria")
        private String breed;

        private int age;

        private String state;

        private String microchip;

        private String veterinaryNotes;

        private String nextVisit;


        private String symptoms;

        @NotBlank(message = "Il peso è obbligatorio")
        private double weight;

        @ManyToOne
        @JoinColumn(name = "cliente_id")
        private Cliente cliente;


        @OneToMany(mappedBy = "animale")
        private Set<Vaccino> vaccini;


        @OneToMany(mappedBy = "animale")
        private Set<Medicine> medicine;

        @OneToMany(mappedBy = "animale")
        private Set<CronologiaAnimale> historicalDiseases;

        @OneToMany(mappedBy = "animale")
        private Set<Operazione> operazione;

        @OneToMany(mappedBy = "animale")
        private Set<Esame> esame;

        @OneToMany(mappedBy = "animale")
        private Set<Trattamento> trattamento;

        @ManyToOne
        @JoinColumn(name = "veterinario_id")
        private VeterinarioDTO veterinario;




}
