package com.vetclinic.repository;

import com.vetclinic.models.Vaccino;
import com.vetclinic.models.Animale;
import com.vetclinic.models.VeterinarioDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccinoRepository extends JpaRepository<Vaccino, Long> {

    List<Vaccino> findByAnimale(Animale animale);

    List<Vaccino> findByVeterinario(VeterinarioDTO veterinario);
}
