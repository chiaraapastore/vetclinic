package com.vetclinic.models;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Date;

public class SomministrazioneRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientId;
    private Long headOfDepartmentId;
    private String nameOfMedicine;
    private int quantity;
    private Date requestDate;
    private String status;
    private Long veterinarianId;

}
