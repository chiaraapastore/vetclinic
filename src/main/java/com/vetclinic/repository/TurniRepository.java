package com.vetclinic.repository;

import com.vetclinic.models.Turni;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurniRepository extends JpaRepository<Turni, Long> {

    List<Turni> findByUtenteId(Long utenteId);

    List<Turni> findByApprovedTrue();


    List<Turni> findByApprovedFalse();
}
