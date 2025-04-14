package com.vetclinic.repository;

import com.vetclinic.models.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Map;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByDepartmentId(Long departmentId);
    List<Map<String, Object>> findConsumoPerReparto();
}
