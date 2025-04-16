package com.vetclinic.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "medicine")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private String dosage;

    private String sideEffects;

    private String expirationDate;

    private int quantity;

    private int currentStock;

    private int pendingOrders;

    private int unitsToReceive;

    private int availableQuantity;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Reparto department;


    @ManyToOne
    @JoinColumn(name = "animale_id")
    private Animale animale;


    @Setter
    @ManyToOne
    @JoinColumn(name = "magazzino_id")
    private Magazzino magazzino;

    public void updateAvailableQuantity(int quantity) {
        this.availableQuantity += quantity;
    }

}