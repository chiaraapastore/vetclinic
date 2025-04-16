package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.Medicine;
import com.vetclinic.models.Utente;
import com.vetclinic.repository.MedicineRepository;
import com.vetclinic.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;

    public MedicineService(MedicineRepository medicineRepository, UtenteRepository utenteRepository, AuthenticationService authenticationService) {
        this.medicineRepository = medicineRepository;
        this.utenteRepository = utenteRepository;
        this.authenticationService = authenticationService;
    }

    @Transactional
    public Optional<Medicine> getMedicineById(Long id) {
        Utente utente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (utente == null) {
            throw new IllegalArgumentException("User not found");
        }
        return medicineRepository.findById(id);
    }

    @Transactional
    public Medicine saveMedicine(Medicine medicine) {
        Utente utente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (utente == null) {
            throw new IllegalArgumentException("User not found");
        }
        return medicineRepository.save(medicine);
    }

    @Transactional
    public void updateMedicineAvailableQuantity(Long medicineId, int newAvailableQuantity) {
        int updatedRows = medicineRepository.updateAvailableQuantity(medicineId, newAvailableQuantity);
        if (updatedRows == 0) {
            throw new RuntimeException("No rows updated! Check if the ID exists.");
        }
        System.out.println("available_quantity updated directly in the DB!");
    }

    @Transactional
    public void deleteMedicine(Long id) {
        Utente utente = utenteRepository.findByUsername(authenticationService.getUsername());
        if (utente == null) {
            throw new IllegalArgumentException("User not found");
        }
        medicineRepository.deleteById(id);
    }

    @Transactional
    public List<Medicine> getAvailableMedicines() {
        return medicineRepository.findByQuantityGreaterThan(0);
    }
}
