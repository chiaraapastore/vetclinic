package com.vetclinic.repository;

import com.vetclinic.models.Operazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperazioneRepository extends JpaRepository<Operazione, Long> {
    List<Operazione> findByAnimaleId(Long animaleId);
}
