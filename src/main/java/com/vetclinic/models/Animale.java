package com.vetclinic.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

        private String microchip;

        @NotBlank(message = "Il peso è obbligatorio")
        private double weight;

        @ManyToOne
        @JoinColumn(name = "cliente_id")
        private Cliente cliente;

}
