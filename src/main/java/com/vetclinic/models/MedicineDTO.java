package com.vetclinic.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineDTO {
    private Long id;
    private String name;
    private String description;
    private String dosage;
    private String expirationDate;
    private int quantity;
    private int availableQuantity;
    private Long departmentId;
    private String departmentName;
}
