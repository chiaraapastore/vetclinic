package com.vetclinic.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Date;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Entity
    @Table(name = "ordine")
    public class Ordine {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JsonIgnore
        @JoinColumn(name = "medicine_id")
        private Medicine medicine;

        private int quantity;

        private Date orderDate;

        private Date deliveryDate;

        private String supplierName;

        @ManyToOne
        @JoinColumn(name = "fornitore_id")
        private Fornitore supplier;

        private OrderStatus status;


        public enum OrderStatus {
            PENDING,
            COMPLETED,
            CANCELED
        }
    }

