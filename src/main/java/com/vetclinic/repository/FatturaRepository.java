package com.vetclinic.repository;

import com.vetclinic.models.Cliente;
import com.vetclinic.models.Fattura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FatturaRepository extends JpaRepository<Fattura, Long> {

    Optional<Fattura> findByCliente(Cliente cliente);
}
