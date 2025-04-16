package com.vetclinic.repository;

import com.vetclinic.models.Animale;
import com.vetclinic.models.Operazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperazioneRepository extends JpaRepository<Operazione, Long> {
    int findByAnimale(Animale animale);
}
