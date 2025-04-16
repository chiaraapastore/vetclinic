package com.vetclinic.repository;

import com.vetclinic.models.Magazzino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MagazzinoRepository extends JpaRepository<Magazzino, Long> {
}
