package com.vetclinic.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "magazzino")
public class Magazzino {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String magazineName;

    private int maximumCapacity;

    private Long medicineId;

    private int currentStock;

    private int pendingOrders;

    private int unitsToReceive;

    private String expirationDate;

    private boolean isAvailable;



}
