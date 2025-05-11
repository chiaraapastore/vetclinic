package com.vetclinic.repository;


import com.vetclinic.models.Reparto;
import com.vetclinic.models.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {
    List<Veterinario> findByRepartoId(Long repartoId);

    List<Veterinario> findByReparto(Reparto reparto);
}
