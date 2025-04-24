package com.vetclinic.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    @JoinColumn(name = "veterinario_id")
    private Veterinario veterinarian;


    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "assistente_id")
    private Assistente assistente;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    private Long animaleId;
    private Long operazioneId;
    private Long trattamentoId;
    private Long esameId;
    private Long vaccinoId;
    private Date eventDate;
    private String eventType;
    private String description;
}

