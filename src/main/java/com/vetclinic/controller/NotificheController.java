package com.vetclinic.controller;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.Notifiche;
import com.vetclinic.service.NotificheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/notifications")
public class NotificheController {

    private final NotificheService notificheService;
    private final AuthenticationService authenticationService;

    public NotificheController(NotificheService notificheService, AuthenticationService authenticationService) {
        this.notificheService = notificheService;
        this.authenticationService = authenticationService;
    }


    @GetMapping("/list")
    public ResponseEntity<List<Notifiche>> getMyNotifications() {
        String keycloakId = authenticationService.getUserId();
        List<Notifiche> notifiche = notificheService.getNotificationsForUserKeycloakId(keycloakId);
        return ResponseEntity.ok(notifiche);
    }




    @GetMapping("/mark-all-read")
    public ResponseEntity<List<Notifiche>> markAllNotificationsAsRead() {
        List<Notifiche> notificheAggiornate = notificheService.markAllNotificationsAsRead();
        return ResponseEntity.ok(notificheAggiornate);
    }


    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        notificheService.sendNotification(message);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-to-user")
    public ResponseEntity<Map<String, String>> sendNotificationToUser(@RequestBody Map<String, Object> payload) {
        Long userId = Long.parseLong(payload.get("userId").toString());
        String message = payload.get("message").toString();
        notificheService.sendNotificationToSpecificUser(userId, message);
        return ResponseEntity.ok(Map.of("message", "Notifica inviata."));
    }







    @DeleteMapping("/delete-all")
    public ResponseEntity<Void> deleteAllNotifications() {
        notificheService.deleteAllNotificationsForCurrentUser();
        return ResponseEntity.noContent().build();
    }

}
