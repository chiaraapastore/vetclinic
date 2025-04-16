package com.vetclinic.repository;

import com.vetclinic.models.Animale;
import com.vetclinic.models.Esame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EsameRepository extends JpaRepository<Esame, Long> {
    List<Esame> findByAnimalId(Long animalId);

    int findByAnimal(Animale animale);
}
