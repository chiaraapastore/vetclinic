package com.vetclinic.repository;

import com.vetclinic.models.Ordine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdineRepository extends JpaRepository<Ordine, Long> {
    List<Ordine> findByStatus(Ordine.OrderStatus orderStatus);
}
