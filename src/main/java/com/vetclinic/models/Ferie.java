package com.vetclinic.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class Ferie {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private boolean approved;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "utente_id")
    private Utente utente;

    public Ferie(LocalDate startDate, LocalDate endDate, Utente user) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.utente = user;
    }
}
