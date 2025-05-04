package com.vetclinic.repository;


import com.vetclinic.models.Animale;
import com.vetclinic.models.Somministrazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SomministrazioneRepository extends JpaRepository<Somministrazione, Long> {
    List<Somministrazione> findAll();
    List<Somministrazione> findByAnimal(Animale animale);
}
