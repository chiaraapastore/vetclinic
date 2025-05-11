package com.vetclinic.models;

import lombok.Data;

@Data
public class NuovoAnimaleDTO {
    private String nomeAnimale;
    private String specie;
    private String razza;
    private double peso;
    private Long repartoId;


    private String clienteNome;
    private String clienteCognome;
    private String clienteEmail;
}
