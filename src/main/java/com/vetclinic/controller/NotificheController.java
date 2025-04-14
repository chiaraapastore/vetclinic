package com.vetclinic.controller;

import com.vetclinic.models.Notifiche;
import com.vetclinic.service.NotificheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/notifications")
public class NotificheController {

    private final NotificheService notificheService;

    public NotificheController(NotificheService notificheService) {
        this.notificheService = notificheService;
    }


    @GetMapping("/mark-all-read")
    public ResponseEntity<List<Notifiche>> markAllNotificationsAsRead() {
        notificheService.markAllNotificationsAsRead();
        return ResponseEntity.ok().build();
    }



}
