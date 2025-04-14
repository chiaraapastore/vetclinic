package com.vetclinic.repository;

import com.vetclinic.models.Magazzino;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MagazzinoRepository extends JpaRepository<Magazzino, Long> {
}
