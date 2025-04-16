package com.vetclinic.models;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Date;

@Data
public class SomministrazioneRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long animalId;
    private Long headOfDepartmentId;
    private String nameOfMedicine;
    private int quantity;
    private Date requestDate;
    private String status;
    private Long veterinarianId;

}
