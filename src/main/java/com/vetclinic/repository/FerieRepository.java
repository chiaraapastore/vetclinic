package com.vetclinic.repository;

import com.vetclinic.models.Ferie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FerieRepository extends JpaRepository<Ferie, Long> {

    List<Ferie> findByUtenteId(Long utenteId);

    List<Ferie> findByApprovedTrue();

    List<Ferie> findByApprovedFalse();

    List<Ferie> findByUtenteRepartoIdAndApprovedTrue(Long repartoId);

    List<Ferie> findByUtenteRepartoIdAndApprovedFalse(Long repartoId);
}
