package com.vetclinic.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "emergenza")
public class Emergenza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "animal_id")
    private Animale animal;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "veterinario_id")
    private Veterinario veterinarian;

    private Date emergencyDate;

    private String description;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    private String dosage;
    
}

