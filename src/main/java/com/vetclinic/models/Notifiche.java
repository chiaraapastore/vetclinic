package com.vetclinic.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notification")
public class Notifiche {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private boolean isRead;

    private Date notificationDate;

    @ManyToOne
    @JoinColumn(name = "animale_id")
    private Animale animal;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente client;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @ManyToOne
    @JoinColumn(name = "sent_by_id")
    private Utente sentBy;

    @ManyToOne
    @JoinColumn(name = "sent_to_id")
    private Utente sentTo;

    public void setLetta(boolean b) {

    }

    public enum NotificationType {
        EXPIRING_MEDICINE,
        MEDICINE_ADMINISTERED,
        GENERAL_ALERT
    }


    public boolean isExpired() {
        return notificationDate.before(new Date());
    }
}
