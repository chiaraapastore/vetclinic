package com.vetclinic.repository;


import com.vetclinic.models.Animale;
import com.vetclinic.models.Cliente;
import com.vetclinic.models.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnimaleRepository extends JpaRepository<Animale, Long> {

    List<Animale> findByVeterinarian(Utente veterinarian);
    List<Animale> findByCliente(Cliente cliente);
}

