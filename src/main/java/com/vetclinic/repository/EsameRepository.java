package com.vetclinic.repository;

import com.vetclinic.models.Animale;
import com.vetclinic.models.Esame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;
@Repository
public interface EsameRepository extends JpaRepository<Esame, Long> {
    List<Esame> findByAnimaleId(Long animalId);


    List<Esame> findByAnimale(Animale animale);
}
