package com.vetclinic.repository;
import com.vetclinic.models.*;


import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UtenteRepository extends JpaRepository<Utente, Long> {

    Utente findByUsername(String username);
    Optional<Utente> findUtenteById(Long id);
    @Query("SELECT u FROM Utente u WHERE u.keycloakId = :keycloakId")
    Optional<Utente> findByKeycloakId(@Param("keycloakId") String keycloakId);

    @Query("SELECT u FROM Utente u WHERE TYPE(u) = Veterinario AND u.id = :veterinarianId")
    Optional<Utente> findByVeterinarianId(Long veterinarianId);
    @Query("SELECT u FROM Utente u WHERE u.reparto.id = :departmentId")
    Collection<Utente> findByDepartmentId(Long departmentId);
    @Query("SELECT u FROM Utente u WHERE u.email = :email AND TYPE(u) = Veterinario")
    Optional<Utente> findVeterinarioByEmail(@Param("email") String email);
    Utente findByEmail(String email);
    Optional<Utente> findByReparto_IdAndRole(Long repartoId, String role);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT a FROM Assistente a WHERE LOWER(a.username) = LOWER(:username)")
    Optional<Assistente> findAssistenteByUsername(@Param("username") String username);

    Optional<Veterinario> findVeterinarioById(Long veterinarianId);

    Optional<CapoReparto> findCapoRepartoById(Long capoRepartoId);

    Optional<Cliente> findClienteByKeycloakId(String userId);

    @Query("SELECT a FROM Assistente a WHERE a.reparto.id = :repartoId")
    List<Assistente> findAssistentiByRepartoId(@Param("repartoId") Long repartoId);

    @Query("SELECT u FROM Utente u WHERE TYPE(u) = CapoReparto")
    List<CapoReparto> findAllCapoReparto();

    List<Utente> findAllByReparto(Reparto reparto);

    List<Utente> findByRepartoId(Long repartoId);

    Optional<Assistente> findAssistenteByKeycloakId(String userId);
}

