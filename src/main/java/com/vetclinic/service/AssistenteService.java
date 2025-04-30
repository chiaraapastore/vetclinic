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
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
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
    private final AnimaleRepository animaleRepository;
    private final FatturaRepository fatturaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final SomministrazioneRepository somministrazioneRepository;

    public AssistenteService(AppuntamentoRepository appuntamentoRepository, MedicineRepository medicineRepository,
                             AnimaleRepository pazienteRepository, NotificheService notificheService, AssistenteRepository assistenteRepository,
                             UtenteRepository utenteRepository, AuthenticationService authenticationService, AnimaleRepository animaleRepository, FatturaRepository fatturaRepository, PagamentoRepository pagamentoRepository, SomministrazioneRepository somministrazioneRepository) {
        this.appuntamentoRepository = appuntamentoRepository;
        this.medicineRepository = medicineRepository;
        this.pazienteRepository = pazienteRepository;
        this.notificheService = notificheService;
        this.utenteRepository = utenteRepository;
        this.authenticationService = authenticationService;
        this.assistenteRepository = assistenteRepository;
        this.animaleRepository = animaleRepository;
        this.fatturaRepository = fatturaRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.somministrazioneRepository = somministrazioneRepository;
    }

    @Transactional
    public Appuntamento createAppointment(Long animalId, Long veterinarianId, Date appointmentDate, String reason) {

        Animale animal = pazienteRepository.findById(animalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animale non trovato"));

        Utente veterinarian = utenteRepository.findByVeterinarianId(veterinarianId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario non trovato"));

        Cliente cliente = animal.getCliente();

        Appuntamento appointment = new Appuntamento();
        appointment.setAnimal(animal);
        appointment.setVeterinarian((Veterinario) veterinarian);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setReason(reason);
        appointment.setCliente(cliente);

        if (animal.getReparto() == null && veterinarian.getReparto() != null) {
            animal.setReparto(veterinarian.getReparto());
            animaleRepository.save(animal);
        }

        Appuntamento savedAppointment = appuntamentoRepository.save(appointment);

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
        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animale non trovato"));
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicinale non trovato"));

        if (medicine.getAvailableQuantity() < quantita) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantità insufficiente del medicinale");
        }

        medicine.setAvailableQuantity(medicine.getAvailableQuantity() - quantita);
        medicineRepository.save(medicine);

        Utente veterinarian = utenteRepository.findById(veterinarianId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario non trovato"));
        notificheService.sendNotificationSomministration(veterinarian, veterinarian, medicine.getName());

        Somministrazione somministrazione = new Somministrazione();
        somministrazione.setAnimal(animale);
        somministrazione.setMedicine(medicine);
        somministrazione.setDosage(quantita);
        somministrazione.setDate(LocalDateTime.now());
        somministrazioneRepository.save(somministrazione);

        return "Farmaco somministrato con successo!";
    }

    @Transactional
    public String managePayment(Long clienteId, double amount, String paymentMethod, String cardType) {
        Cliente cliente = (Cliente) utenteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente non trovato"));

        Fattura fattura = new Fattura();
        fattura.setCliente(cliente);
        fattura.setIssueDate(new Date());
        fattura.setAmount(amount);
        fattura.setStatus("PENDING");
        fatturaRepository.save(fattura);

        Pagamento pagamento = new Pagamento();
        pagamento.setFattura(fattura);
        pagamento.setAmount(amount);
        pagamento.setPaymentMethod(paymentMethod);
        pagamento.setPaymentDate(new Date());
        pagamento.setCardType(cardType);
        pagamento.setStatus("SUCCESS");
        pagamentoRepository.save(pagamento);

        fattura.setStatus("PAID");
        fatturaRepository.save(fattura);


        notificheService.sendAppointmentReminder(cliente, fattura.getIssueDate());

        return "Pagamento effettuato con successo!";
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

}
