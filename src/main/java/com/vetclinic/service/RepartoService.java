package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.Reparto;
import com.vetclinic.models.Utente;
import com.vetclinic.repository.RepartoRepository;
import com.vetclinic.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RepartoService {

    private final RepartoRepository departmentRepository;
    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;

    public RepartoService(RepartoRepository departmentRepository,
                          UtenteRepository utenteRepository,
                          AuthenticationService authenticationService) {
        this.departmentRepository = departmentRepository;
        this.utenteRepository = utenteRepository;
        this.authenticationService = authenticationService;
    }

    public List<Reparto> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional
    public Reparto createDepartment(Reparto department) {
        return departmentRepository.save(department);
    }

    @Transactional
    public Reparto updateDepartment(Long id, Reparto updatedDepartment) {
        Reparto department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reparto non trovato"));

        Utente loggedUser = utenteRepository.findByUsername(authenticationService.getUsername());

        if (!loggedUser.getRole().equals("admin") &&
                !loggedUser.getRole().equals("capo-reparto") &&
                !department.getHeadOfDepartment().equals(loggedUser)) {
            throw new RuntimeException("Non hai i permessi per aggiornare questo reparto.");
        }

        department.setName(updatedDepartment.getName());
        department.setHeadOfDepartment(updatedDepartment.getHeadOfDepartment());
        return departmentRepository.save(department);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Reparto department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reparto non trovato"));

        Utente loggedUser = utenteRepository.findByUsername(authenticationService.getUsername());

        if (!loggedUser.getRole().equals("admin") &&
                !loggedUser.getRole().equals("capo-reparto") &&
                !department.getHeadOfDepartment().equals(loggedUser)) {
            throw new RuntimeException("Non hai i permessi per cancellare questo reparto.");
        }

        departmentRepository.deleteById(id);
    }

    @Transactional
    public Reparto findDepartmentById(Long departmentId) {
        Reparto department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Dipartimento non trovato"));

        Utente loggedUser = utenteRepository.findByUsername(authenticationService.getUsername());

        if (!loggedUser.getRole().equals("admin") &&
                !loggedUser.getRole().equals("capo-reparto") &&
                !department.getHeadOfDepartment().equals(loggedUser) &&
                !department.getAssistente().equals(loggedUser) &&
                !department.getVeterinario().equals(loggedUser)) {
            throw new RuntimeException("Non hai i permessi per visualizzare questo reparto.");
        }

        return department;
    }
}
