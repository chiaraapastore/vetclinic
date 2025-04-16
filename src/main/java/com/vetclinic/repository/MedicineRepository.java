package com.vetclinic.repository;

import com.vetclinic.models.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByDepartmentId(Long departmentId);
    List<Map<String, Object>> findConsumoPerReparto();
    Optional<Medicine> findByName(String nomeMedicinale);
    int updateAvailableQuantity(Long medicineId, int newAvailableQuantity);
    List<Medicine> findByQuantityGreaterThan(int i);
    Collection<Object> findByAvailableQuantityLessThanEqual(int i);
}
