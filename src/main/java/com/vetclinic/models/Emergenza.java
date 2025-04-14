package com.vetclinic.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "emergenza")
public class Emergenza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "animal_id")
    private Animale animal;

    @ManyToOne
    @JoinColumn(name = "veterinario_id")
    private VeterinarioDTO veterinarian;

    private Date emergencyDate;

    private String description;

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    private String dosage;

    public boolean isMedicineStockCritical() {
        if (medicine != null && medicine.getAvailableQuantity() <= 0) {
            return true;
        }
        return false;
    }
}

