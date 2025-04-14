package com.vetclinic.models;

import jakarta.persistence.*;

@Entity
@Table(name = "magazzino")
public class Magazzino {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String magazineName;

    private int maximumCapacity;

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    private int currentStock;

    private int pendingOrders;

    private int unitsToReceive;

    private String expirationDate;

    private boolean isAvailable;

    public int calculateAvailableStock() {
        return currentStock - pendingOrders + unitsToReceive;
    }

    public boolean isMedicineOutOfStock(Medicine medicine) {
        return calculateAvailableStock() <= 0;
    }

}
