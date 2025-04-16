package com.vetclinic.repository;

import com.vetclinic.models.CronologiaAnimale;
import com.vetclinic.models.Animale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CronologiaRepository extends JpaRepository<CronologiaAnimale, Long> {

    List<CronologiaAnimale> findByAnimal(Animale animal);
}
