package com.vetclinic.repository;

import com.vetclinic.models.Notifiche;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificheRepository extends JpaRepository<Notifiche, Long> {
    List<Notifiche> findBySentToIdAndIsReadFalse(Long id);
    List<Notifiche> findBySentToId(Long id);
}
