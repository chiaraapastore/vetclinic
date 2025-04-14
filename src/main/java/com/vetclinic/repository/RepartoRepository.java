package com.vetclinic.repository;

import com.vetclinic.models.Reparto;

import com.vetclinic.models.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RepartoRepository extends JpaRepository<Reparto, Long> {

    Optional<Reparto> findFirstByNome(String repartoNome);

    Optional<Reparto> findByCapoReparto(Utente utente);
}
