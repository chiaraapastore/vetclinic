package com.vetclinic.service;


import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.NotificheRepository;
import com.vetclinic.repository.UtenteRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.fasterxml.jackson.databind.type.LogicalType.DateTime;

@Service
public class NotificheService {

    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;
    private final NotificheRepository notificheRepository;
    private final TaskScheduler taskScheduler;

    public NotificheService(UtenteRepository utenteRepository, TaskScheduler taskScheduler,NotificheRepository notificheRepository, AuthenticationService authenticationService) {
        this.utenteRepository = utenteRepository;
        this.notificheRepository = notificheRepository;
        this.authenticationService = authenticationService;
        this.taskScheduler = taskScheduler;
    }

    @Transactional
    public void sendWelcomeNotification(Utente utenteAdmin, Utente newUser) {
        String message = "Benvenuto, " + newUser.getFirstName() + "! Sei stato registrato come " + newUser.getRole() + ".";
        createAndSendNotification(utenteAdmin, newUser, message, "welcome");
    }



    @Transactional
    public void notifyDepartmentChange(Utente user, String newDepartmentName, Utente headOfDepartment) {
        Utente sender = utenteRepository.findByUsername(authenticationService.getUsername());
        if (sender == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }
        String message = "Sei stato spostato in un nuovo reparto! Ora fai parte del reparto " + newDepartmentName + ".";
        createAndSendNotification(sender, user, message, "department_change");
        if (headOfDepartment != null) {
            String chiefMessage = user.getFirstName() + " " + user.getLastName() + " è stato spostato nel reparto " + newDepartmentName + ".";
            createAndSendNotification(sender, headOfDepartment, chiefMessage, "department_change");
        }
    }

    @Transactional
    public List<Notifiche> markAllNotificationsAsRead() {
        Utente user = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato");
        }
        List<Notifiche> unreadNotifications = notificheRepository.findBySentToIdAndIsReadFalse(user.getId());

        if (!unreadNotifications.isEmpty()) {
            unreadNotifications.forEach(notification -> notification.setRead(true));
            notificheRepository.saveAll(unreadNotifications);
            user.setCountNotification(0);
            utenteRepository.save(user);
        }

        return notificheRepository.findBySentToId(user.getId());
    }

    @Transactional
    public void sendNotificationTurni(Utente destinatario, Utente autore, LocalDate inizio, LocalDate fine) {
        if (inizio.equals(fine)) {
            throw new IllegalArgumentException("La data di inizio e di fine non possono essere uguali.");
        }

        String messaggio = String.format(
                "Il sottoscritto %s %s dovrà coprire i turni dal giorno %s al giorno %s",
                destinatario.getFirstName(), destinatario.getLastName(), inizio, fine
        );

        Notifiche notifica = new Notifiche();
        notifica.setMessage(messaggio);
        notifica.setSentTo(destinatario);
        notifica.setSentBy(autore);
        notifica.setNotificationDate(new Date());
        notificheRepository.save(notifica);
    }



    @Transactional
    public void sendVeterinarianNotificationToHeadOfDepartment(Utente veterinarian, Utente headOfDepartment, String nameOfMedicine) {
        String message = "Attenzione, il farmaco"+nameOfMedicine+" è scaduto";
        createAndSendNotification(veterinarian, headOfDepartment, message, "farmaco_scaduto");
    }


    @Transactional
    public void sendNotificationSomministration(Utente veterinarian, Utente headOfDepartment, String nameOfMedicine){
        Utente admin = utenteRepository.findAll().stream()
                .filter(u -> u.getRole().equals("admin"))
                .findFirst()
                .orElse(null);

        if (admin != null) {
            notifyAdmin("È stata effettuata una nuova somministrazione da " + veterinarian.getUsername());
        }

        String message = "Il dottore "+veterinarian.getFirstName()+veterinarian.getLastName()+" ha somministrato il farmaco "+nameOfMedicine+" al paziente";
        createAndSendNotification(veterinarian,headOfDepartment, message, "farmaco");
    }

    @Transactional
    public void sendAppointmentCanceledNotification(Cliente owner) {
        String message = "Notifica: L'appuntamento per " + owner.getFirstName() + " " + owner.getLastName() + " è stato cancellato.";
        Utente assistant = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));
        if (assistant == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }
        sendNotificationFromAssistantToClient(owner, message, Notifiche.NotificationType.GENERAL_ALERT);
    }

    @Transactional
    public void sendAppointmentReminder(Cliente owner, Date appointmentDate) {
        String message = "Promemoria: L'appuntamento per " + owner.getFirstName()  + owner.getLastName() +  " è fissato per " + appointmentDate;

        sendNotificationFromAssistantToClient(owner, message, Notifiche.NotificationType.GENERAL_ALERT);
    }


    @Transactional
    public void sendAppointmentReminderToVeterinarian(Veterinario veterinarian, Cliente owner, Date appointmentDate) {
        String message = "Promemoria: Hai un appuntamento con il paziente di " + owner.getFirstName() + " " + owner.getLastName() + " fissato per " + appointmentDate;
        Utente assistant = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));
        if (assistant == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }
        sendNotificationFromAssistantToVeterinarian(veterinarian, message, Notifiche.NotificationType.GENERAL_ALERT);
    }

    private void sendNotificationFromAssistantToVeterinarian(Veterinario veterinarian, String message, Notifiche.NotificationType notificationType) {
        Notifiche notification = new Notifiche();
        notification.setMessage(message);
        notification.setRead(false);
        notification.setNotificationDate(new Date());
        notification.setType(notificationType);

        Utente assistant = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));
        if (assistant == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }

        notification.setSentBy(assistant);
        notification.setSentTo(veterinarian);

        notificheRepository.save(notification);
    }



    @Transactional
    public void sendEmergencyNotificationToHeadOfDepartment(Veterinario veterinarian, Utente headOfDepartment, String description, Medicine medicine) {
        String message = "Emergenza segnalata dal veterinario " + veterinarian.getFirstName()  + veterinarian.getLastName() +
                ": " + description + ". Farmaco richiesto: " + medicine.getName() + ". Disponibilità: " + medicine.getAvailableQuantity();
        createAndSendNotification(veterinarian, headOfDepartment, message, "emergenza");
    }

    @Transactional
    public void sendVeterinarianNotificationToDepartmentHead(Utente assistant, Utente departmentHead, String medicineName) {
        String message = "Notifica: Il farmaco " + medicineName + " sta per scadere. Notifica inviata da "
                + assistant.getUsername() + " al Capo Reparto " + departmentHead.getUsername();
        createAndSendNotification(assistant, departmentHead, message, "farmaco_scaduto");
    }


    void createAndSendNotification(Utente sender, Utente receiver, String message, String type) {
        if (receiver == null) {
            return;
        }
        Notifiche notification = new Notifiche();
        notification.setMessage(message);
        notification.setSentBy(sender);
        notification.setSentTo(receiver);
        notification.setNotificationDate(new Date());

        try {
            notification.setType(Notifiche.NotificationType.valueOf(type.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo notifica non valido: " + type);
        }

        notification.setRead(false);
        notificheRepository.save(notification);

        receiver.setCountNotification(
                (receiver.getCountNotification() != null ? receiver.getCountNotification() : 0) + 1
        );
        utenteRepository.save(receiver);
    }


    public void sendNotificationFromAssistantToClient(Cliente client, String message, Notifiche.NotificationType type) {
        if (client == null) {
            return;
        }
        Utente assistant = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assistente non autenticato"));

        Notifiche notification = new Notifiche();
        notification.setMessage(message);
        notification.setClient(client);
        notification.setSentTo(client);
        notification.setSentBy(assistant);
        notification.setNotificationDate(new Date());
        notification.setType(type);
        notification.setRead(false);

        notificheRepository.save(notification);

    }


    @Transactional
    public void sendEmergencyNotificationToAssistant(Veterinario veterinarian, Utente assistant, String description, Medicine medicine) {

        String message = "Emergenza: " + veterinarian.getFirstName() + veterinarian.getLastName() + " ha segnalato un'emergenza per l'animale " +
                veterinarian.getReparto().getName() + ". Descrizione: " + description + ". Farmaco coinvolto: " + medicine.getName();
        createAndSendNotification(veterinarian, assistant, message, "emergenza");
    }

    @Transactional
    public void sendPaymentNotificationToClient(Cliente client, Pagamento payment) {
        String message = "Il pagamento di €" + payment.getAmount() + " per il servizio richiesto è stato completato con successo. Data del pagamento: " + payment.getPaymentDate();
        createAndSendNotificationPayment(client, message, Notifiche.NotificationType.PAYMENT_CONFIRMATION);
    }


    @Transactional
    public void sendPaymentNotificationToVeterinarian(Veterinario veterinarian, Pagamento payment) {
        String message = "Il pagamento di €" + payment.getAmount() + " per l'appuntamento con il cliente " + veterinarian.getFirstName() + veterinarian.getLastName() + " è stato completato con successo.";
        createAndSendNotificationPayment(veterinarian, message,Notifiche.NotificationType.PAYMENT_CONFIRMATION);
    }

    private void createAndSendNotificationPayment(Utente receiver, String message, Notifiche.NotificationType type) {
        if (receiver == null) return;

        Notifiche notification = new Notifiche();
        notification.setMessage(message);
        notification.setSentTo(receiver);
        notification.setNotificationDate(new Date());
        notification.setType(type);
        notification.setRead(false);

        notificheRepository.save(notification);

        receiver.setCountNotification(
                (receiver.getCountNotification() != null ? receiver.getCountNotification() : 0) + 1
        );
        utenteRepository.save(receiver);
    }



    @Transactional

    public void sendNotificationAnomalia(String referenzaId, String tipoAnomalia) {
        String message = "Anomalia rilevata per la referenza ID: " + referenzaId + " - Tipo: " + tipoAnomalia;

        Utente sender = utenteRepository.findByUsername(authenticationService.getUsername());
        if (sender == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }


        Utente veterinarian = utenteRepository.findByDepartmentId(sender.getReparto().getId()).stream()
                .filter(utente -> utente instanceof Veterinario)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Veterinario non trovato"));

        createAndSendNotification(sender, veterinarian, message, "anomalia");

        Utente headOfDepartment = utenteRepository.findByDepartmentId(veterinarian.getReparto().getId()).stream()
                .filter(utente -> utente.getRole().equals("capo-reparto"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Capo reparto non trovato"));

        createAndSendNotification(veterinarian, headOfDepartment, message, "anomalia");


    }

    public List<Notifiche> getAllNotificationsForCurrentUser() {
        Utente user = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));
        return notificheRepository.findBySentToId(user.getId());
    }


    @Transactional
    public void deleteAllNotificationsForCurrentUser() {
        Utente user = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato");
        }
        notificheRepository.deleteBySentToId(user.getId());
        user.setCountNotification(0);
        utenteRepository.save(user);
    }


    @Async
    public void sendAppointmentReminderAtScheduledTime(Cliente cliente, Veterinario veterinario, LocalDateTime reminderTime) {
        taskScheduler.schedule(() -> {
            sendAppointmentReminder(cliente, new Date());
        }, java.util.Date.from(reminderTime.atZone(java.time.ZoneId.systemDefault()).toInstant()));
    }

    public void sendAssistantNotificationToCapoReparto(Assistente assistente, CapoReparto capoReparto, String name) {
        String message = "Notifica: Il farmaco " +name + " sta per scadere. Notifica inviata da "
                + assistente.getUsername() + " al Capo Reparto " + capoReparto.getUsername();
        createAndSendNotification(assistente, capoReparto, message, "farmaco_scaduto");
    }


    @Transactional
    public void notifyAdmin(String messaggio) {

        Utente admin = utenteRepository.findAll().stream()
                .filter(u -> u.getRole().equalsIgnoreCase("admin"))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin non trovato nel sistema"));

        Utente sender = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mittente non autenticato"));

        Notifiche notifica = new Notifiche();
        notifica.setMessage(messaggio);
        notifica.setNotificationDate(new Date());
        notifica.setType(Notifiche.NotificationType.GENERAL_ALERT);
        notifica.setRead(false);
        notifica.setSentTo(admin);
        notifica.setSentBy(sender);

        notificheRepository.save(notifica);


        admin.setCountNotification((admin.getCountNotification() != null ? admin.getCountNotification() : 0) + 1);
        utenteRepository.save(admin);
    }








    public void notifyFerieAssegnate(Utente destinatario, LocalDate startDate, LocalDate endDate, Utente capoReparto) {
        Notifiche notifica = new Notifiche();
        notifica.setSentTo(destinatario);
        notifica.setSentBy(capoReparto);
        notifica.setMessage("Ti sono state assegnate le ferie dal giorno: " + startDate + " al giorno: " + endDate);
        notifica.setNotificationDate(new Date());
        notifica.setRead(false);
        notifica.setType(Notifiche.NotificationType.TURNI);

        notificheRepository.save(notifica);
    }


    @Transactional
    public void sendNotification(String message) {
        Utente sender =utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        if (sender == null) {
            throw new IllegalArgumentException("Utente autenticato non trovato");
        }

        Utente receiver = sender;

        Notifiche notifica = new Notifiche();
        notifica.setMessage(message);
        notifica.setSentBy(sender);
        notifica.setSentTo(receiver);
        notifica.setNotificationDate(new Date());
        notifica.setType(Notifiche.NotificationType.GENERAL_ALERT);
        notifica.setRead(false);

        notificheRepository.save(notifica);

        receiver.setCountNotification(
                (receiver.getCountNotification() != null ? receiver.getCountNotification() : 0) + 1
        );
        utenteRepository.save(receiver);
    }


    @Transactional
    public void sendNotificationToSpecificUser(Long userId, String message) {
        Utente sender = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente autenticato non trovato"));

        Utente receiver = utenteRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destinatario non trovato"));

        createAndSendNotification(sender, receiver, message, "GENERAL_ALERT");
    }


    @Transactional
    public void sendNotificationToAssistente(Long repartoId, @NotBlank(message = "Il messaggio è obbligatorio") String messaggio) {
        List<Assistente> assistenti = utenteRepository.findAssistentiByRepartoId(repartoId);

        if (assistenti.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nessun assistente trovato nel reparto con ID: " + repartoId);
        }

        for (Assistente assistente : assistenti) {
            Notifiche notifica = new Notifiche();
            notifica.setSentTo(assistente);
            notifica.setMessage(messaggio);
            notifica.setNotificationDate(new Date());
            notifica.setType(Notifiche.NotificationType.GENERAL_ALERT);
            notifica.setRead(false);
            notificheRepository.save(notifica);
        }
    }

    public void eliminaNotifichePerUtente(Utente utente) {
        notificheRepository.deleteBySentTo(utente);
        notificheRepository.deleteBySentBy(utente);
    }



    public List<Notifiche> getNotificationsForUserKeycloakId(String keycloakId) {
        Utente utente = utenteRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));
        return notificheRepository.findBySentToIdOrderByNotificationDateDesc(utente.getId());
    }

}
