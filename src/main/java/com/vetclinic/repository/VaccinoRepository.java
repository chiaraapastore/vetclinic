package com.vetclinic.repository;

import com.vetclinic.models.Vaccino;
import com.vetclinic.models.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccinoRepository extends JpaRepository<Vaccino, Long> {

    List<Vaccino> findByAnimaleId(Long animaleId);

    List<Vaccino> findByVeterinario(Veterinario veterinario);
}
