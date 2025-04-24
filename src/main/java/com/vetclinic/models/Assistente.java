package com.vetclinic.models;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@DiscriminatorValue("assistente")
public class Assistente extends Utente {
}
