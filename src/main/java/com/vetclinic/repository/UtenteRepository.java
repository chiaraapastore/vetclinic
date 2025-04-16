package com.vetclinic.repository;
import com.vetclinic.models.Reparto;
import com.vetclinic.models.Utente;


import com.vetclinic.models.VeterinarioDTO;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface UtenteRepository extends JpaRepository<Utente, Long> {

    Utente findByUsername(String username);
    Optional<Utente> findByKeycloakId(String keycloakId);
    @Query("SELECT u FROM Utente u WHERE TYPE(u) = VeterinarioDTO AND u.id = :veterinarianId")
    Optional<Utente> findByVeterinarianId(Long veterinarianId);
    @Query("SELECT u FROM Utente u WHERE u.reparto.id = :departmentId")
    Collection<Utente> findByDepartmentId(Long departmentId);
    @Query("SELECT u FROM Utente u WHERE u.email = :email AND TYPE(u) = VeterinarioDTO")
    Optional<Reparto> findRepartoByEmailVeterinarian(@Param("email") String email);
    Utente findByEmail(String email);
}

