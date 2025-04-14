package com.vetclinic.repository;
import com.vetclinic.models.Reparto;
import com.vetclinic.models.Utente;


import com.vetclinic.models.VeterinarioDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface UtenteRepository extends JpaRepository<Utente, Long> {

    Utente findByUsername(String username);
    Optional<Utente> findByKeycloakId(String keycloakId);
    Optional<VeterinarioDTO> findByVeterinarianId(Long veterinarianId);
    Collection<Utente> findByDepartmentId(Long departmentId);
    Optional<Reparto> findRepartoByEmailVeterinarian(String emailDottore);
    Utente findByEmail(String email);
}

