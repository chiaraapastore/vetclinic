package com.vetclinic.repository;

import com.vetclinic.models.Reparto;

import com.vetclinic.models.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Repository
public interface RepartoRepository extends JpaRepository<Reparto, Long> {

    Optional<Reparto> findFirstByName(String repartoName);
    Optional<Reparto> findByCapoRepartoId(Long capoRepartoId);

}
