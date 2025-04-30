package com.vetclinic.repository;

import com.vetclinic.models.Appuntamento;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppuntamentoRepository extends JpaRepository<Appuntamento, Long> {

    List<Appuntamento> findByAnimalId(Long animalId);

    List<Appuntamento> findByVeterinarianId(Long veterinarianId);

    @Query("SELECT a FROM Appuntamento a WHERE a.animal.reparto.id = :repartoId")
    List<Appuntamento> findByAnimalRepartoId(@Param("repartoId") Long repartoId);


}
