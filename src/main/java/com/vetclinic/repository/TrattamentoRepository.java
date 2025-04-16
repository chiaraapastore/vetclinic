package com.vetclinic.repository;

import com.vetclinic.models.Animale;
import com.vetclinic.models.Trattamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrattamentoRepository extends JpaRepository<Trattamento, Long> {
    List<Trattamento> findByAnimaleId(Long animalId);

    int findByAnimale(Animale animale);
}
