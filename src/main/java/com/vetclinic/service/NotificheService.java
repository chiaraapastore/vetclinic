package com.vetclinic.service;


import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.Cliente;
import com.vetclinic.models.Notifiche;
import com.vetclinic.models.Utente;
import com.vetclinic.repository.NotificheRepository;
import com.vetclinic.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class NotificheService {

    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;
    private final NotificheRepository notificheRepository;

    public NotificheService(UtenteRepository utenteRepository, NotificheRepository notificheRepository, AuthenticationService authenticationService) {
        this.utenteRepository = utenteRepository;
        this.notificheRepository = notificheRepository;
        this.authenticationService = authenticationService;
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
        Utente user = utenteRepository.findByUsername(authenticationService.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }
        List<Notifiche> unreadNotifications = notificheRepository.findByReceiverIdAndLettaFalse(user.getId());

        if (!unreadNotifications.isEmpty()) {

            unreadNotifications.forEach(notification -> notification.setLetta(true));
            notificheRepository.saveAll(unreadNotifications);

            user.setCountNotification(0);
            utenteRepository.save(user);
        }

        return notificheRepository.findByReceiverId(user.getId());
    }

    @Transactional
    public void sendNotificationTurni(Utente veterinarian, Utente headOfDepartment, LocalDate startDate, LocalDate endDate) {
        String message = "Il sottoscritto"+veterinarian.getFirstName()+" "+veterinarian.getLastName()+"dovrà coprire i turni dal giorno"+startDate+"al giorno"+endDate;
        createAndSendNotification(veterinarian,headOfDepartment, message, "turni");
    }

    @Transactional
    public void sendVeterinarianNotificationToHeadOfDepartment(Utente veterinarian, Utente headOfDepartment, String nameOfMedicine) {
        String message = "Attenzione, il farmaco"+nameOfMedicine+" è scaduto";
        createAndSendNotification(veterinarian, headOfDepartment, message, "farmaco_scaduto");
    }


    @Transactional
    public void sendNotificationSomministration(Utente veterinarian, Utente headOfDepartment, String nameOfMedicine){
        String message = "Il dottore"+veterinarian.getFirstName()+" "+veterinarian.getLastName()+"ha somministrato il farmaco"+nameOfMedicine+"al paziente";
        createAndSendNotification(veterinarian,headOfDepartment, message, "farmaco");
    }

    @Transactional
    public void sendAppointmentCanceledNotification(Cliente owner) {
        String message = "Notifica: L'appuntamento per " + owner.getFirstName() + " " + owner.getLastName() + " è stato cancellato.";
        Utente assistant = utenteRepository.findByUsername(authenticationService.getUsername());
        if (assistant == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }
        sendNotificationFromAssistantToClient(owner, message, Notifiche.NotificationType.GENERAL_ALERT);
    }

    @Transactional
    public void sendAppointmentReminder(Cliente owner, Date appointmentDate) {
        String message = "Promemoria: L'appuntamento per " + owner.getFirstName() + " " + owner.getLastName() +  " è fissato per " + appointmentDate;
        Utente assistant = utenteRepository.findByUsername(authenticationService.getUsername());
        if (assistant == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }
        sendNotificationFromAssistantToClient(owner, message, Notifiche.NotificationType.GENERAL_ALERT);
    }

    @Transactional
    public void sendVeterinarianNotificationToDepartmentHead(Utente assistant, Utente departmentHead, String medicineName) {
        String message = "Notifica: Il farmaco " + medicineName + " sta per scadere. Notifica inviata da "
                + assistant.getUsername() + " al Capo Reparto " + departmentHead.getUsername();
        createAndSendNotification(assistant, departmentHead, message, "farmaco_scaduto");
    }


    private void createAndSendNotification(Utente sender, Utente receiver, String message, String type) {
        if (receiver == null) {
            return;
        }
        Notifiche notification = new Notifiche();
        notification.setMessage(message);
        notification.setSentBy(sender);
        notification.setSentTo(receiver);
        notification.setNotificationDate(new Date());
        notification.setType(Notifiche.NotificationType.valueOf(type));
        notification.setRead(false);

        notificheRepository.save(notification);

        receiver.setCountNotification(receiver.getCountNotification() + 1);
        utenteRepository.save(receiver);
    }


    private void sendNotificationFromAssistantToClient(Cliente client, String message, Notifiche.NotificationType type) {

        Utente assistant = utenteRepository.findByUsername(authenticationService.getUsername());
        if (assistant == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }
        createAndSendNotification(assistant, client, message, type.toString());
    }

}
