package com.vetclinic.repository;

import com.vetclinic.models.Appuntamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AppuntamentoRepository extends JpaRepository<Appuntamento, Long> {

    List<Appuntamento> findByAnimalId(Long animalId);

    List<Appuntamento> findByVeterinarianId(Long veterinarianId);

}
