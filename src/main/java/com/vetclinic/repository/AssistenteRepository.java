package com.vetclinic.repository;

import com.vetclinic.models.Assistente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssistenteRepository extends JpaRepository<Assistente, Long> {
    List<Assistente> findByFirstNameAndLastName(String firstName, String lastName);
    List<Assistente> findByRepartoId(Long repartoId);
}