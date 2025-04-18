package com.vetclinic.repository;


import com.vetclinic.models.Animale;
import com.vetclinic.models.Cliente;
import com.vetclinic.models.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimaleRepository extends JpaRepository<Animale, Long> {

    List<Animale> findByCliente(Cliente cliente);

    List<Animale> findByVeterinario(Utente veterinario);
}

