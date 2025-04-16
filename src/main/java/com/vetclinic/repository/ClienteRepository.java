package com.vetclinic.repository;

import com.vetclinic.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findById(Long id);

    Optional<Cliente> findByUsername(String username);

    Optional<Cliente> findByEmail(String email);
}
