package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.Reparto;
import com.vetclinic.models.Utente;
import com.vetclinic.repository.NotificheRepository;
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
    private final RepartoRepository repartoRepository;
    private final KeycloakService keycloakService;
    private final NotificheRepository notificationRepository;

    public RepartoService(RepartoRepository departmentRepository,
                          UtenteRepository utenteRepository,
                          RepartoRepository repartoRepository,
                          KeycloakService keycloakService,
                          NotificheRepository notificationRepository,
                          AuthenticationService authenticationService) {
        this.departmentRepository = departmentRepository;
        this.utenteRepository = utenteRepository;
        this.authenticationService = authenticationService;
        this.repartoRepository = repartoRepository;
        this.keycloakService = keycloakService;
        this.notificationRepository = notificationRepository;
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
                !(loggedUser.getReparto()!= null && loggedUser.getReparto().getId().equals(department.getId()))){
             throw new RuntimeException("Non hai i permessi per aggiornare questo reparto.");
        }

        department.setName(updatedDepartment.getName());
        return departmentRepository.save(department);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Reparto department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reparto non trovato"));

        Utente loggedUser = utenteRepository.findByUsername(authenticationService.getUsername());

        boolean isAdmin = authenticationService.getUsername().equals("admin");
        boolean isCapoReparto = loggedUser.getRole().equals("capo-reparto") && loggedUser.getReparto()!=null && loggedUser.getReparto().getId().equals(id);

       if(!isAdmin && !isCapoReparto){
           throw new RuntimeException("Non hai i permessi per cancellare questo reparto.");
       }

        departmentRepository.delete(department);
    }

    @Transactional
    public Reparto findDepartmentById(Long departmentId) {
        Reparto department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Dipartimento non trovato"));

        Utente loggedUser = utenteRepository.findByUsername(authenticationService.getUsername());

        boolean isAdmin = loggedUser.getRole().equals("admin");
        boolean isAssignedToReparto = loggedUser.getReparto() != null && loggedUser.getReparto().getId().equals(departmentId);

        if (!isAdmin && !isAssignedToReparto) {
            throw new RuntimeException("Non hai i permessi per visualizzare questo reparto.");
        }

        return department;
    }

    @Transactional
    public void eliminaRepartoConUtenti(Long repartoId) {
        Reparto reparto = repartoRepository.findById(repartoId)
                .orElseThrow(() -> new RuntimeException("Reparto non trovato con ID: " + repartoId));

        if (reparto.getCapoRepartoId() != null) {
            utenteRepository.findById(reparto.getCapoRepartoId()).ifPresent(capo -> {
                try {
                    notificationRepository.deleteBySentToId(capo.getId());
                    notificationRepository.deleteBySentBy(capo);
                    keycloakService.deleteUserByUsername(capo.getUsername());
                } catch (Exception e) {
                    System.out.println("Errore durante eliminazione da Keycloak (capo reparto): " + e.getMessage());
                }
            });
        }

        List<Utente> utentiDelReparto = utenteRepository.findByRepartoId(repartoId);
        for (Utente utente : utentiDelReparto) {
            try {
                notificationRepository.deleteBySentTo(utente);
                notificationRepository.deleteBySentBy(utente);
                keycloakService.deleteUserByUsername(utente.getUsername());
                utenteRepository.deleteById(utente.getId());
            } catch (Exception e) {
                System.out.println("Errore durante eliminazione da Keycloak o DB (utente): " + e.getMessage());
                e.printStackTrace();
            }

        }

        repartoRepository.delete(reparto);
    }


}
