package com.vetclinic.service;


import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
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
        List<Notifiche> unreadNotifications = notificheRepository.findBySentToIdAndIsReadFalse(user.getId());

        if (!unreadNotifications.isEmpty()) {

            unreadNotifications.forEach(notification -> notification.setLetta(true));
            notificheRepository.saveAll(unreadNotifications);

            user.setCountNotification(0);
            utenteRepository.save(user);
        }

        return notificheRepository.findBySentToId(user.getId());
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
    public void sendEmergencyNotificationToHeadOfDepartment(VeterinarioDTO veterinarian, Utente headOfDepartment, String description, Medicine medicine) {
        String message = "Emergenza segnalata dal veterinario " + veterinarian.getFirstName() + " " + veterinarian.getLastName() +
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
        notification.setType(Notifiche.NotificationType.valueOf(type));
        notification.setRead(false);

        notificheRepository.save(notification);

        receiver.setCountNotification(receiver.getCountNotification() + 1);
        utenteRepository.save(receiver);
    }


    private void sendNotificationFromAssistantToClient(Cliente client, String message, Notifiche.NotificationType type) {

    }

    @Transactional
    public void sendEmergencyNotificationToAssistant(VeterinarioDTO veterinarian, Utente assistant, String description, Medicine medicine) {

        String message = "Emergenza: " + veterinarian.getFirstName() + " " + veterinarian.getLastName() + " ha segnalato un'emergenza per l'animale " +
                veterinarian.getReparto().getName() + ". Descrizione: " + description + ". Farmaco coinvolto: " + medicine.getName();
        createAndSendNotification(veterinarian, assistant, message, "emergenza");
    }

    @Transactional
    public void sendPaymentNotificationToClient(Cliente client, Pagamento payment) {
        String message = "Il pagamento di €" + payment.getAmount() + " per il servizio richiesto è stato completato con successo. Data del pagamento: " + payment.getPaymentDate();
        createAndSendNotifications(client, message, "payment_confirmation");
    }


    @Transactional
    public void sendPaymentNotificationToVeterinarian(VeterinarioDTO veterinarian, Pagamento payment) {
        String message = "Il pagamento di €" + payment.getAmount() + " per l'appuntamento con il cliente " + veterinarian.getFirstName() + " " + veterinarian.getLastName() + " è stato completato con successo.";
        createAndSendNotifications(veterinarian, message, "payment_confirmation");
    }

    private void createAndSendNotifications(Utente receiver, String message, String type) {
        if (receiver == null) {
            return;
        }
        Notifiche notification = new Notifiche();
        notification.setMessage(message);
        notification.setSentTo(receiver);
        notification.setNotificationDate(new Date());
        notification.setType(Notifiche.NotificationType.valueOf(type));
        notification.setRead(false);

        notificheRepository.save(notification);

        receiver.setCountNotification(receiver.getCountNotification() + 1);
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
                .filter(utente -> utente instanceof VeterinarioDTO)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Veterinario non trovato"));

        createAndSendNotification(sender, veterinarian, message, "anomalia");

        Utente headOfDepartment = utenteRepository.findByDepartmentId(veterinarian.getReparto().getId()).stream()
                .filter(utente -> utente.getRole().equals("capo-reparto"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Capo reparto non trovato"));

        createAndSendNotification(veterinarian, headOfDepartment, message, "anomalia");


    }


}
