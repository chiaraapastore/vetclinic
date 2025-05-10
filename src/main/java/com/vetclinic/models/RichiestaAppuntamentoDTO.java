package com.vetclinic.models;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class RichiestaAppuntamentoDTO {
        public Long animalId;
        public String motivo;


        @NotNull
        public LocalDateTime dataRichiesta;

}
