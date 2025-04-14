package com.vetclinic.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CapoRepartoDTO {
    private Long repartoId;
    private Reparto department;
    private Utente capoReparto;

    public CapoRepartoDTO(Long repartoId, Reparto department, Utente capoReparto) {
        this.repartoId = repartoId;
        this.department = department;
        this.capoReparto = capoReparto;
    }
}
