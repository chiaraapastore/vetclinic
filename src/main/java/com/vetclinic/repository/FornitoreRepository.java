package com.vetclinic.repository;

import com.vetclinic.models.Fornitore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FornitoreRepository extends JpaRepository<Fornitore, Long> {
     Optional<Fornitore> findById(Long fornitoreId);
}
