package com.vetclinic.repository;

import com.vetclinic.models.CronologiaAnimale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CronologiaRepository extends JpaRepository<CronologiaAnimale, Long> {

    @Query("SELECT c FROM CronologiaAnimale c WHERE c.animaleId = :animaleId ORDER BY c.eventDate ASC")
    List<CronologiaAnimale> findByAnimaleId(Long animaleId);
}
