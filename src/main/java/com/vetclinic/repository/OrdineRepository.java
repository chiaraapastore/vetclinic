package com.vetclinic.repository;

import com.vetclinic.models.Ordine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdineRepository extends JpaRepository<Ordine, Long> {
    List<Ordine> findByStatus(Ordine.OrderStatus orderStatus);
}
