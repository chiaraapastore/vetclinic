package com.vetclinic.repository;

import com.vetclinic.models.Medicine;
import feign.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByDepartmentId(Long departmentId);
    @Query("SELECT m.department.name AS department, SUM(m.availableQuantity) AS totalConsumption " +
            "FROM Medicine m GROUP BY m.department.name")
    List<Map<String, Object>> findConsumoPerReparto();
    Optional<Medicine> findByName(String nomeMedicinale);

    List<Medicine> findByQuantityGreaterThan(int i);
    Collection<Object> findByAvailableQuantityLessThanEqual(int i);
    @Modifying
    @Transactional
    @Query("UPDATE Medicine m SET m.availableQuantity = :newAvailableQuantity WHERE m.id = :medicinaleId")
    int updateAvailableQuantity(@Param("medicinaleId") Long medicinaleId, @Param("newAvailableQuantity") int newAvailableQuantity);


    @Query("SELECT m FROM Medicine m WHERE m.availableQuantity = 0")
    List<Medicine> findMedicinesInEmergenza();
}
