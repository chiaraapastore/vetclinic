package com.vetclinic.repository;

import com.vetclinic.models.Cliente;
import com.vetclinic.models.Fattura;
import com.vetclinic.models.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FatturaRepository extends JpaRepository<Fattura, Long> {


    List<Fattura> findByCliente(Utente cliente);
    Optional<Fattura> findFirstByCliente(Cliente cliente);
}
