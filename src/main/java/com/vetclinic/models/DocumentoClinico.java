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
    @JoinColumn(name = "animal_id")
    private Animale animal;

    @ManyToOne
    @JoinColumn(name = "veterinario_id")
    private VeterinarioDTO veterinario;

    @ManyToOne
    @JoinColumn(name = "assistente_id")
    private Assistente assistant;
}
