package com.vetclinic.models;


import jakarta.persistence.*;

import lombok.Data;


@Data
@Entity
@DiscriminatorValue("cliente")
public class Cliente extends Utente{

}

