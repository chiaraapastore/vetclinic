package com.vetclinic.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    @JoinColumn(name = "veterinario_id")
    private Veterinario veterinario;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "cronologiaAnimale")
    private CronologiaAnimale cronologiaAnimale;

    private Long animaleId;

    private String tipoOperazione;

    private String descrizione;

    private LocalDateTime dataOra;

}
