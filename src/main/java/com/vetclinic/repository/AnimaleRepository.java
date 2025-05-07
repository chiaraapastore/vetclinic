package com.vetclinic.repository;


import com.vetclinic.models.Animale;
import com.vetclinic.models.Cliente;
import com.vetclinic.models.Utente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimaleRepository extends JpaRepository<Animale, Long> {

    List<Animale> findByCliente(Cliente cliente);

    List<Animale> findByVeterinario(Utente veterinario);


    List<Animale> findByVeterinarioIn(List<Utente> veterinariNelReparto);

    List<Animale> findByClienteId(Long id);

    Animale findFirstByOrderByIdAsc();

    List<Animale> findByRepartoId(Long repartoId);
}

