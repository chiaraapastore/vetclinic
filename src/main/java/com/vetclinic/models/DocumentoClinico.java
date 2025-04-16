package com.vetclinic.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "documento_clinico")
public class DocumentoClinico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String documentType;
    private String documentName;
    private String documentPath;
    @ManyToOne
    @JoinColumn(name = "animale_id")
    private Animale animale;

    @ManyToOne
    @JoinColumn(name = "veterinario_id")
    private Utente veterinario;

    @ManyToOne
    @JoinColumn(name = "assistente_id")
    private Assistente assistant;
}
