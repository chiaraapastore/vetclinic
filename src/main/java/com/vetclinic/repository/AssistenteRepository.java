package com.vetclinic.repository;

import com.vetclinic.models.Assistente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssistenteRepository extends JpaRepository<Assistente, Long> {
    List<Assistente> findByFirstNameAndLastName(String firstName, String lastName);
    List<Assistente> findByRepartoId(Long repartoId);
}