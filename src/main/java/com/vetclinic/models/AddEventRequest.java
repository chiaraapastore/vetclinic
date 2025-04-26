package com.vetclinic.models;


import lombok.Data;

@Data
public class AddEventRequest {
    private String eventType;
    private String description;
}
