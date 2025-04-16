package com.vetclinic.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reparto")
public class Reparto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String name;

    private int countMedicinal;


    @OneToOne
    @JoinColumn(name = "capo_reparto_id")
    private Utente headOfDepartment;

    @ManyToOne
    @JoinColumn(name = "assistente_id")
    private Assistente assistente;

    @OneToOne
    @JoinColumn(name = "veterinario_id")
    private VeterinarioDTO veterinario;



}