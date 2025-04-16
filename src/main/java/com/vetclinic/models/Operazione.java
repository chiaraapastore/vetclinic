package com.vetclinic.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Operazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "animal_id")
    private Animale animale;

    @ManyToOne
    @JoinColumn(name = "veterinario_id")
    private Utente veterinario;

    @ManyToOne
    @JoinColumn(name = "animale_id")
    private CronologiaAnimale cronologiaAnimale;

    private String tipoOperazione;

    private String descrizione;

    private LocalDateTime dataOra;

}
