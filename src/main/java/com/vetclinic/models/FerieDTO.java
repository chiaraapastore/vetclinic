package com.vetclinic.models;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FerieDTO {
    private Long id;
    private String startDate;
    private String endDate;
    private UtenteDTO utente;

    public FerieDTO(Ferie ferie) {
        this.id = ferie.getId();
        this.startDate = ferie.getStartDate().toString();
        this.endDate = ferie.getEndDate().toString();
        this.utente = new UtenteDTO(ferie.getUtente());
    }
}