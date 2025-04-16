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
@Table(name = "vaccino")
public class Vaccino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private Date administrationDate;

    @ManyToOne
    @JoinColumn(name = "animal_id")
    private Animale animale;

    @ManyToOne
    @JoinColumn(name = "veterinario_id")
    private VeterinarioDTO veterinario;

    @ManyToOne
    @JoinColumn(name = "animale_id")
    private CronologiaAnimale cronologiaAnimale;
}
