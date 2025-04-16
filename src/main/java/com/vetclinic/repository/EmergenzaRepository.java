package com.vetclinic.repository;

import com.vetclinic.models.Emergenza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmergenzaRepository extends JpaRepository<Emergenza, Long> {

}
