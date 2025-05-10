package com.vetclinic.repository;

import com.vetclinic.models.Cliente;
import com.vetclinic.models.RichiestaAppuntamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RichiestaAppuntamentoRepository extends JpaRepository<RichiestaAppuntamento, Long> {

    List<RichiestaAppuntamento> findByApprovatoFalseAndRifiutatoFalse();

    List<RichiestaAppuntamento> findByCliente(Cliente cliente);
}
