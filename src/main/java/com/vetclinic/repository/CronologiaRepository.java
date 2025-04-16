package com.vetclinic.repository;

import com.vetclinic.models.CronologiaAnimale;
import com.vetclinic.models.Animale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CronologiaRepository extends JpaRepository<CronologiaAnimale, Long> {

    List<CronologiaAnimale> findByAnimale(Animale animale);
}
