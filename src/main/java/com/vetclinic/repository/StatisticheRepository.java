package com.vetclinic.repository;

import com.vetclinic.models.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface StatisticheRepository extends JpaRepository<Medicine, Long> {

}
