package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssistenteService {

    private final AppuntamentoRepository appuntamentoRepository;
    private final MedicineRepository medicineRepository;
    private final AnimaleRepository pazienteRepository;
    private final NotificheService notificheService;
    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;
    private final AssistenteRepository assistenteRepository;
    private final RepartoRepository repartoRepository;
    private final AnimaleRepository animaleRepository;
    private final MagazzinoRepository magazzinoRepository;
    private final SomministrazioneRepository somministrazioneRepository;
    private final SomministrazioneService somministrazioneService;
    private final RichiestaAppuntamentoRepository richiestaAppuntamentoRepository;


    public AssistenteService(AppuntamentoRepository appuntamentoRepository, RichiestaAppuntamentoRepository richiestaAppuntamentoRepository,MedicineRepository medicineRepository,
                             AnimaleRepository pazienteRepository, NotificheService notificheService, AssistenteRepository assistenteRepository,
                             UtenteRepository utenteRepository, RepartoRepository repartoRepository, AuthenticationService authenticationService, AnimaleRepository animaleRepository, MagazzinoRepository magazzinoRepository, SomministrazioneRepository somministrazioneRepository, SomministrazioneService somministrazioneService) {
        this.appuntamentoRepository = appuntamentoRepository;
        this.medicineRepository = medicineRepository;
        this.pazienteRepository = pazienteRepository;
        this.notificheService = notificheService;
        this.utenteRepository = utenteRepository;
        this.authenticationService = authenticationService;
        this.assistenteRepository = assistenteRepository;
        this.animaleRepository = animaleRepository;
        this.magazzinoRepository = magazzinoRepository;
        this.somministrazioneRepository = somministrazioneRepository;
        this.somministrazioneService = somministrazioneService;
        this.repartoRepository = repartoRepository;
        this.richiestaAppuntamentoRepository = richiestaAppuntamentoRepository;
    }

    @Transactional
    public Appuntamento createAppointment(Long animalId, Long veterinarianId, Date appointmentDate, String reason, Double amount) {

        Animale animal = pazienteRepository.findById(animalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animale non trovato"));

        Utente veterinarian = utenteRepository.findByVeterinarianId(veterinarianId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario non trovato"));


        Appuntamento appointment = new Appuntamento();
        appointment.setAnimal(animal);
        appointment.setVeterinarian((Veterinario) veterinarian);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setReason(reason);
        appointment.setCliente(animal.getCliente());
        appointment.setStatus("DA PAGARE");
        appointment.setAmount(amount);

        if (animal.getReparto() == null && veterinarian.getReparto() != null) {
            animal.setReparto(veterinarian.getReparto());
            animaleRepository.save(animal);
        }

        Appuntamento savedAppointment = appuntamentoRepository.save(appointment);

        String message = "Hai un nuovo appuntamento per " + reason +
                " il " + appointmentDate.toString() +
                " con il dott. " + veterinarian.getFirstName() + " " + veterinarian.getLastName();
        notificheService.sendNotificationFromAssistantToClient(
                animal.getCliente(),
                message,
                Notifiche.NotificationType.GENERAL_ALERT
        );

        LocalDateTime reminderTime = appointmentDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime()
                .minusDays(1);

        notificheService.sendAppointmentReminderAtScheduledTime(
                animal.getCliente(),
                (Veterinario) veterinarian,
                reminderTime
        );

        return savedAppointment;
    }


    @Transactional
    public void deleteAppointment(Long appointmentId) {
        Utente assistant = utenteRepository.findByUsername(authenticationService.getUsername());
        if (assistant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assistente non trovato");
        }
        Appuntamento appointment = appuntamentoRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appuntamento non trovato"));
        appuntamentoRepository.delete(appointment);
        notificheService.sendAppointmentCanceledNotification(appointment.getAnimal().getCliente());
    }

    public void remindAppointment(Long appointmentId, String newDate) {
        Appuntamento appuntamento = appuntamentoRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appuntamento non trovato"));

        LocalDateTime dateTime = LocalDateTime.parse(newDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());

        appuntamento.setAppointmentDate(date);
        appuntamentoRepository.save(appuntamento);
    }


    @Transactional
    public void checkMedicineExpiration(Long departmentHeadId, Long medicineId) {
        Utente assistant = utenteRepository.findByUsername(authenticationService.getUsername());
        if (assistant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assistente non trovato");
        }
        Utente departmentHead = utenteRepository.findById(departmentHeadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capo Reparto non trovato"));
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicinale non trovato"));
        notificheService.sendVeterinarianNotificationToDepartmentHead(assistant, departmentHead, medicine.getName());
    }

    @Transactional(readOnly = true)
    public List<MedicineDTO> viewDepartmentMedicines(Long departmentId) {
        List<Medicine> medicines = medicineRepository.findByDepartmentId(departmentId);

        return medicines.stream().map(medicine -> new MedicineDTO(
                medicine.getId(),
                medicine.getName(),
                medicine.getDescription(),
                medicine.getDosage(),
                medicine.getExpirationDate(),
                medicine.getQuantity(),
                medicine.getAvailableQuantity(),
                medicine.getDepartment() != null ? medicine.getDepartment().getId() : null,
                medicine.getDepartment() != null ? medicine.getDepartment().getName() : null
        )).collect(Collectors.toList());
    }




    @Transactional
    public List<Animale> getVeterinarianPatients() {

        Utente assistente = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assistente non trovato"));

        Long repartoId = assistente.getReparto().getId();


        List<Utente> veterinariNelReparto = utenteRepository.findByDepartmentId(repartoId).stream()
                .filter(u -> "veterinario".equalsIgnoreCase(u.getRole()))
                .toList();

        return animaleRepository.findByVeterinarioIn(veterinariNelReparto);
    }


    @Transactional
    public Assistente getAssistenteById(Long id) {
        return assistenteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assistente non trovato con ID: " + id));
    }

    @Transactional
    public List<Assistente> getAllAssistenti() {
        return assistenteRepository.findAll();
    }

    @Transactional
    public List<Assistente> getAssistentiByName(String firstName, String lastName) {
        return assistenteRepository.findByFirstNameAndLastName(firstName, lastName);
    }

    @Transactional
    public List<Assistente> getAssistentiByReparto(Long repartoId) {
        return assistenteRepository.findByRepartoId(repartoId);
    }

    @Transactional
    public List<Veterinario> getVeterinariesByDepartment(Long departmentId) {
        return utenteRepository.findByDepartmentId(departmentId).stream()
                .filter(utente -> "veterinario".equalsIgnoreCase(utente.getRole()))
                .map(veterinarian -> {
                    Veterinario veterinario = new Veterinario();
                    veterinario.setId(veterinarian.getId());
                    veterinario.setFirstName(veterinarian.getFirstName());
                    veterinarian.setLastName(veterinarian.getLastName());
                    veterinario.setEmail(veterinarian.getEmail());
                    veterinario.setRegistrationNumber(veterinarian.getRegistrationNumber());
                    veterinario.setSpecialization(((Veterinario) veterinarian).getSpecialization());
                    veterinario.setAvailable(((Veterinario) veterinarian).getAvailable());
                    veterinario.setReparto(veterinarian.getReparto());
                    return veterinario;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Reparto getRepartoByVeterinarian(String emailDottore) {
        Utente veterinario = utenteRepository.findVeterinarioByEmail(emailDottore)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nessun dottore trovato con email: " + emailDottore));

        return veterinario.getReparto();
    }

    @Transactional
    public String administerMedicine(Long animaleId, Long medicineId, int quantita, Long veterinarianId) {
        Assistente assistant = utenteRepository.findAssistenteByUsername(authenticationService.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utente non è un assistente"));

        Veterinario veterinarian = utenteRepository.findVeterinarioById(veterinarianId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario non trovato"));

        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animale non trovato"));

        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicinale non trovato"));

        if (medicine.getAvailableQuantity() < quantita) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantità insufficiente del medicinale");
        }

        medicine.setAvailableQuantity(medicine.getAvailableQuantity() - quantita);
        medicineRepository.save(medicine);

        notificheService.sendNotificationSomministration(assistant, veterinarian, medicine.getName());

        Somministrazione somministrazione = new Somministrazione();
        somministrazione.setAnimal(animale);
        somministrazione.setMedicine(medicine);
        somministrazione.setDosage(quantita);
        somministrazione.setDate(LocalDateTime.now());
        somministrazione.setAssistente(assistant);
        somministrazione.setVeterinario(veterinarian);
        medicine.setDepartment(animale.getReparto());
        somministrazioneRepository.save(somministrazione);

        somministrazioneService.addDocumentToAnimal(
                animaleId,
                veterinarianId,
                assistant.getId(),
                "SOMMINISTRAZIONE",
                "Somministrazione farmaco: " + medicine.getName(),
                null
        );

        return "Farmaco somministrato con successo!";
    }


    @Transactional(readOnly = true)
    public List<Medicine> getEmergenze() {
        return medicineRepository.findMedicinesInEmergenza();
    }



    @Transactional
    public List<Appuntamento> getAppointmentsForRepartoOfAssistant() {
        Assistente assistente = utenteRepository.findAssistenteByUsername(authenticationService.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assistente non trovato"));

        if (assistente.getReparto() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assistente senza reparto assegnato");
        }

        Long repartoId = assistente.getReparto().getId();
        List<Appuntamento> appuntamenti = appuntamentoRepository.findByAnimalRepartoId(repartoId);

        return appuntamenti;
    }

    @Transactional
    public String aggiungiFarmaco(Map<String, Object> payload) {
        String nome = (String) payload.get("nome");
        Integer quantita = (Integer) payload.get("quantita");
        Integer availableQuantity = (Integer) payload.get("availableQuantity");
        String dosage = (String) payload.get("dosage");
        String expirationDate = (String) payload.get("expirationDate");
        String categoria = (String) payload.get("categoria");
        String descrizione = (String) payload.get("descrizione");
        Long departmentId = payload.get("departmentId") != null ? Long.valueOf(payload.get("departmentId").toString()) : null;
        Long magazineId = payload.get("magazineId") != null ? Long.valueOf(payload.get("magazineId").toString()) : null;

        if (nome == null || nome.isEmpty() || quantita == null || quantita <= 0) {
            throw new IllegalArgumentException("Nome del farmaco e quantità devono essere validi.");
        }

        Medicine nuovoFarmaco = new Medicine();
        nuovoFarmaco.setName(nome);
        nuovoFarmaco.setQuantity(quantita);
        nuovoFarmaco.setDosage(dosage);
        nuovoFarmaco.setAvailableQuantity(availableQuantity);
        nuovoFarmaco.setExpirationDate(expirationDate);
        nuovoFarmaco.setCategory(categoria);
        nuovoFarmaco.setDescription(descrizione);

        if (departmentId != null) {
            Reparto department = repartoRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Dipartimento non trovato"));
            nuovoFarmaco.setDepartment(department);
        }

        if (magazineId != null) {
            Magazzino magazine = magazzinoRepository.findById(magazineId)
                    .orElseThrow(() -> new IllegalArgumentException("Magazzino non trovato"));
            nuovoFarmaco.setMagazzino(magazine);
        }

        if (availableQuantity == null || availableQuantity < 0) {
            throw new IllegalArgumentException("La quantità disponibile deve essere specificata e non negativa.");
        }

        medicineRepository.save(nuovoFarmaco);
        return "Farmaco aggiunto con successo!";
    }

    @Transactional
    public void scadenzaFarmaco(Long capoRepartoId, Long medicinaleId) {

        Utente capoReparto = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capo Reparto non trovato"));

        Utente assistente = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assistente non trovato"));


        Medicine medicinale = medicineRepository.findById(medicinaleId)
                .orElseThrow(() -> new IllegalArgumentException("Medicinale non trovato"));

        notificheService.notifyAdmin("L'assistente " + assistente.getUsername() +
                " ha segnalato farmaci scaduti: " + medicinale.getName());

        notificheService.sendAssistantNotificationToCapoReparto(assistente, capoReparto, medicinale.getName());
    }



    public List<Assistente> findByRepartoId(Long repartoId) {
        return assistenteRepository.findByRepartoId(repartoId);
    }

    @Transactional
    public void updateAmount(Long appointmentId, Double amount) {
        Appuntamento appuntamento = appuntamentoRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appuntamento non trovato"));
        appuntamento.setAmount(amount);
        appuntamentoRepository.save(appuntamento);
    }

    @Transactional
    public void richiestaAppuntamentoCliente(RichiestaAppuntamentoDTO dto) {
        Cliente cliente = utenteRepository.findClienteByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente non trovato"));

        Animale animale = animaleRepository.findById(dto.animalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animale non trovato"));

        if (dto.dataRichiesta == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data richiesta mancante");
        }

        Date dataRichiesta = Date.from(dto.dataRichiesta.atZone(ZoneId.systemDefault()).toInstant());

        RichiestaAppuntamento richiesta = new RichiestaAppuntamento();
        richiesta.setCliente(cliente);
        richiesta.setAnimale(animale);
        richiesta.setMotivo(dto.motivo);
        richiesta.setDataRichiesta(dataRichiesta);
        richiesta.setApprovato(false);
        richiesta.setRifiutato(false);

        richiestaAppuntamentoRepository.save(richiesta);

        notificheService.sendNotificationToAssistente(animale.getReparto().getId(),
                "Il cliente " + cliente.getFirstName() + " ha richiesto un appuntamento per il " + dto.dataRichiesta);
    }



    @Transactional(readOnly = true)
    public List<RichiestaAppuntamento> getRichiesteNonApprovate() {
        return richiestaAppuntamentoRepository.findByApprovatoFalseAndRifiutatoFalse();
    }


    @Transactional
    public Appuntamento approvaRichiestaAppuntamento(Long richiestaId) {
        RichiestaAppuntamento richiesta = richiestaAppuntamentoRepository.findById(richiestaId)
                .orElseThrow(() -> new IllegalArgumentException("Richiesta non trovata"));

        if (Boolean.TRUE.equals(richiesta.getApprovato()) || Boolean.TRUE.equals(richiesta.getRifiutato())) {
            throw new IllegalArgumentException("Richiesta già gestita");
        }

        richiesta.setApprovato(true);
        richiesta.setRifiutato(false);

        Animale animale = richiesta.getAnimale();
        Veterinario veterinario = animale.getVeterinario();
        Cliente cliente = richiesta.getCliente();

        Appuntamento appuntamento = new Appuntamento();
        appuntamento.setAnimal(animale);
        appuntamento.setVeterinarian(veterinario);
        appuntamento.setCliente(cliente);
        appuntamento.setAppointmentDate(richiesta.getDataRichiesta());
        appuntamento.setReason(richiesta.getMotivo());
        appuntamento.setAmount(30.0);
        appuntamento.setStatus("CREATED");
        appuntamento.setDate(LocalDateTime.now());

        richiestaAppuntamentoRepository.save(richiesta);
        appuntamentoRepository.save(appuntamento);

        notificheService.sendNotificationFromAssistantToClient(cliente,
                "La tua richiesta per un appuntamento il " + richiesta.getDataRichiesta() + " è stata approvata.",
                Notifiche.NotificationType.GENERAL_ALERT
        );


        notificheService.sendAppointmentReminderToVeterinarian(veterinario, cliente, richiesta.getDataRichiesta());

        return appuntamento;
    }




    @Transactional
    public void rifiutaRichiestaAppuntamento(Long id) {
        RichiestaAppuntamento richiesta = richiestaAppuntamentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Richiesta non trovata"));

        if (Boolean.TRUE.equals(richiesta.getApprovato()) || Boolean.TRUE.equals(richiesta.getRifiutato())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La richiesta è già stata gestita.");
        }

        richiesta.setRifiutato(true);
        richiestaAppuntamentoRepository.save(richiesta);

        Cliente cliente = richiesta.getCliente();
        Date dataRichiesta = richiesta.getDataRichiesta();

        notificheService.sendNotificationFromAssistantToClient(
                cliente,
                "La tua richiesta di appuntamento per il " + dataRichiesta + " è stata rifiutata.",
                Notifiche.NotificationType.GENERAL_ALERT
        );
    }


}
