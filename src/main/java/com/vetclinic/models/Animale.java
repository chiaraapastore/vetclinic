package com.vetclinic.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "animale")

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
        private Double weight;

        @ManyToOne
        @JoinColumn(name = "cliente_id")
        private Cliente cliente;

        @ManyToOne
        @JsonIgnore
        @JoinColumn(name = "vaccino")
        private Vaccino vaccini;

        @ManyToOne
        @JsonIgnore
        @JoinColumn(name = "medicine")
        private Medicine medicine;


        @ManyToOne
        @JsonIgnore
        @JoinColumn(name = "operazione")
        private Operazione operazione;

        @ManyToOne
        @JsonIgnore
        @JoinColumn(name = "esame")
        private Esame esame;

        @ManyToOne
        @JsonIgnore
        @JoinColumn(name = "trattamento")
        private Trattamento trattamento;

        @ManyToOne
        @JoinColumn(name = "veterinario_id")
        private Veterinario veterinario;


        @ManyToOne
        @JoinColumn(name = "reparto_id")
        private Reparto reparto;




}
