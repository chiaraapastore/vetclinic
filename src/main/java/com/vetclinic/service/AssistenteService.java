package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.AppuntamentoRepository;
import com.vetclinic.repository.AssistenteRepository;
import com.vetclinic.repository.MedicineRepository;
import com.vetclinic.repository.AnimaleRepository;
import com.vetclinic.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    public AssistenteService(AppuntamentoRepository appuntamentoRepository, MedicineRepository medicineRepository,
                             AnimaleRepository pazienteRepository, NotificheService notificheService, AssistenteRepository assistenteRepository,
                             UtenteRepository utenteRepository, AuthenticationService authenticationService) {
        this.appuntamentoRepository = appuntamentoRepository;
        this.medicineRepository = medicineRepository;
        this.pazienteRepository = pazienteRepository;
        this.notificheService = notificheService;
        this.utenteRepository = utenteRepository;
        this.authenticationService = authenticationService;
        this.assistenteRepository = assistenteRepository;
    }

    @Transactional
    public Appuntamento createAppointment(Long animalId, Long veterinarianId, Date appointmentDate, String reason) {
        Utente assistant = utenteRepository.findByUsername(authenticationService.getUsername());
        if (assistant == null) {
            throw new IllegalArgumentException("Assistente non trovato");
        }
        Animale animal = pazienteRepository.findById(animalId)
                .orElseThrow(() -> new IllegalArgumentException("Animale non trovato"));

        VeterinarioDTO veterinarian = utenteRepository.findByVeterinarianId(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinario non trovato"));

        Appuntamento appointment = new Appuntamento();
        appointment.setAnimal(animal);
        appointment.setVeterinarian(veterinarian);
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
        return pazienteRepository.findByVeterinarian(veterinarian);
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
                    veterinarianDTO.setDepartment(veterinarian.getReparto());
                    return veterinarianDTO;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Reparto getRepartoByVeterinarian(String emailDottore) {
        return utenteRepository.findRepartoByEmailVeterinarian(emailDottore)
                .orElseThrow(() -> new RuntimeException("Nessun dottore trovato con email: " + emailDottore));
    }
}
