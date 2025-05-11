package com.vetclinic.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@Table(name = "turni")
public class Turni {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private LocalDate startTime;
    private LocalDate endTime;
    private boolean approved;

    @ManyToOne
    @JoinColumn(name = "utente_id")
    private Utente utente;

    public Turni(LocalDate startTime, LocalDate endTime, Utente utente) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.utente = utente;
        this.approved = false;
    }
}
