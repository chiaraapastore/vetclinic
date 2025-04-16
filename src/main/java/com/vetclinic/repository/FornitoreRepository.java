package com.vetclinic.repository;

import com.vetclinic.models.Fornitore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FornitoreRepository extends JpaRepository<Fornitore, Long> {
     Optional<Fornitore> findById(Long fornitoreId);
}
