package com.vetclinic.controller;

import com.vetclinic.models.Notifiche;
import com.vetclinic.service.NotificheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/notifications")
public class NotificheController {

    private final NotificheService notificheService;

    public NotificheController(NotificheService notificheService) {
        this.notificheService = notificheService;
    }


    @GetMapping("/list")
    public ResponseEntity<List<Notifiche>> getAllNotificationsForUser() {
        List<Notifiche> notifiche = notificheService.getAllNotificationsForCurrentUser();
        return ResponseEntity.ok(notifiche);
    }


    @GetMapping("/mark-all-read")
    public ResponseEntity<List<Notifiche>> markAllNotificationsAsRead() {
        List<Notifiche> notificheAggiornate = notificheService.markAllNotificationsAsRead();
        return ResponseEntity.ok(notificheAggiornate);
    }


    @DeleteMapping("/delete-all")
    public ResponseEntity<Void> deleteAllNotifications() {
        notificheService.deleteAllNotificationsForCurrentUser();
        return ResponseEntity.noContent().build();
    }

}
