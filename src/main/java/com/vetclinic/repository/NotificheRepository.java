package com.vetclinic.repository;

import com.vetclinic.models.Notifiche;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificheRepository extends JpaRepository<Notifiche, Long> {
    List<Notifiche> findByReceiverIdAndLettaFalse(Long id);
    List<Notifiche> findByReceiverId(Long id);
}
