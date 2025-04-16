package com.vetclinic.repository;

import com.vetclinic.models.Ferie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FerieRepository extends JpaRepository<Ferie, Long> {

    List<Ferie> findByUtenteId(Long utenteId);

    List<Ferie> findByApprovedTrue();

    List<Ferie> findByApprovedFalse();

    List<Ferie> findByUtenteRepartoIdAndApprovedTrue(Long repartoId);

    List<Ferie> findByUtenteRepartoIdAndApprovedFalse(Long repartoId);
}
