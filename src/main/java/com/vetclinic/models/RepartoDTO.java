package com.vetclinic.models;

import lombok.Data;

@Data
public class RepartoDTO {

    private String repartoNome;
    private UtenteDTO capoReparto;
    private UtenteDTO assistente;
    private UtenteDTO veterinario;

}
