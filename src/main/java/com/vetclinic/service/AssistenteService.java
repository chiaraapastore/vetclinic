package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
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
        Utente assistant = utenteRepository.findByUsername(authenticationService.getUsername());
        if (assistant == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }
        Animale animal = pazienteRepository.findById(animalId)
                .orElseThrow(() -> new IllegalArgumentException("Animale non trovato"));

        Utente veterinarian = utenteRepository.findByVeterinarianId(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinario non trovato"));

        Appuntamento appointment = new Appuntamento();
        appointment.setAnimal(animal);
        appointment.setVeterinarian((VeterinarioDTO) veterinarian);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setReason(reason);

        return appuntamentoRepository.save(appointment);
    }


    @Transactional
    public void deleteAppointment(Long appointmentId) {
        Utente assistant = utenteRepository.findByUsername(authenticationService.getUsername());
        if (assistant == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }
        Appuntamento appointment = appuntamentoRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appuntamento non trovato"));
        appuntamentoRepository.delete(appointment);
        notificheService.sendAppointmentCanceledNotification(appointment.getAnimal().getCliente());
    }

    @Transactional
    public void remindAppointment(Long appointmentId) {
        Utente assistant = utenteRepository.findByUsername(authenticationService.getUsername());
        if (assistant == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }
        Appuntamento appointment = appuntamentoRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appuntamento non trovato"));
        notificheService.sendAppointmentReminder(appointment.getAnimal().getCliente(), appointment.getAppointmentDate());
    }


    @Transactional
    public void checkMedicineExpiration(Long departmentHeadId, Long medicineId) {
        Utente assistant = utenteRepository.findByUsername(authenticationService.getUsername());
        if (assistant == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }
        Utente departmentHead = utenteRepository.findById(departmentHeadId)
                .orElseThrow(() -> new IllegalArgumentException("Capo Reparto non trovato"));
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("Medicinale non trovato"));
        notificheService.sendVeterinarianNotificationToDepartmentHead(assistant, departmentHead, medicine.getName());
    }


    @Transactional
    public List<Medicine> viewDepartmentMedicines(Long departmentId) {
        Utente assistant = utenteRepository.findByUsername(authenticationService.getUsername());
        if (assistant == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }
        List<Medicine> medicines = medicineRepository.findByDepartmentId(departmentId);
        medicines.forEach(medicine -> medicine.setDescription(null));
        return medicines;
    }

    @Transactional
    public List<Animale> getVeterinarianPatients() {
        Utente veterinarian = utenteRepository.findByUsername(authenticationService.getUsername());
        if (veterinarian == null) {
            throw new IllegalArgumentException("Veterinario non trovato");
        }
        return pazienteRepository.findByVeterinario(veterinarian);
    }


    @Transactional
    public Assistente getAssistenteById(Long id) {
        return assistenteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assistente non trovato con ID: " + id));
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
    public List<VeterinarioDTO> getVeterinariesByDepartment(Long departmentId) {
        return utenteRepository.findByDepartmentId(departmentId).stream()
                .filter(utente -> "veterinario".equalsIgnoreCase(utente.getRole()))
                .map(veterinarian -> {
                    VeterinarioDTO veterinarianDTO = new VeterinarioDTO();
                    veterinarianDTO.setId(veterinarian.getId());
                    veterinarianDTO.setFirstName(veterinarian.getFirstName());
                    veterinarianDTO.setLastName(veterinarian.getLastName());
                    veterinarianDTO.setEmail(veterinarian.getEmail());
                    veterinarianDTO.setRegistrationNumber(veterinarian.getRegistrationNumber());
                    veterinarianDTO.setSpecialization(((VeterinarioDTO) veterinarian).getSpecialization());
                    veterinarianDTO.setAvailable(((VeterinarioDTO) veterinarian).isAvailable());
                    veterinarianDTO.setReparto(veterinarian.getReparto());
                    return veterinarianDTO;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Reparto getRepartoByVeterinarian(String emailDottore) {
        return utenteRepository.findRepartoByEmailVeterinarian(emailDottore)
                .orElseThrow(() -> new RuntimeException("Nessun dottore trovato con email: " + emailDottore));
    }



    @Transactional
    public String administerMedicine(Long animaleId, Long medicineId, int quantita, Long veterinarianId) {
        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new IllegalArgumentException("Animale non trovato"));
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("Medicinale non trovato"));

        if (medicine.getAvailableQuantity() < quantita) {
            throw new IllegalArgumentException("Quantità insufficiente del medicinale");
        }


        medicine.setAvailableQuantity(medicine.getAvailableQuantity() - quantita);
        medicineRepository.save(medicine);


        Utente veterinarian = utenteRepository.findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinario non trovato"));
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
                .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato"));


        Fattura fattura = new Fattura();
        fattura.setCliente(cliente);
        fattura.setIssueDate(new Date());
        fattura.setAmount(amount);
        fattura.setStatus("PENDING");
        fatturaRepository.save(fattura);


        Pagamento pagamento = new Pagamento();
        pagamento.setInvoice(fattura);
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
}
