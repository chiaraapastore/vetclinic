package com.vetclinic.repository;

import com.vetclinic.models.Animale;
import com.vetclinic.models.Trattamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrattamentoRepository extends JpaRepository<Trattamento, Long> {
    List<Trattamento> findByAnimalId(Long animalId);

    int findByAnimal(Animale animale);
}
