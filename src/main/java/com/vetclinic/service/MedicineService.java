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
    public Medicine updateMedicinale(Medicine updatedMedicine) {
        Medicine existing = medicineRepository.findById(updatedMedicine.getId())
                .orElseThrow(() -> new IllegalArgumentException("Medicinale non trovato con ID: " + updatedMedicine.getId()));

        existing.setName(updatedMedicine.getName());
        existing.setQuantity(updatedMedicine.getQuantity());
        existing.setAvailableQuantity(updatedMedicine.getAvailableQuantity());
        existing.setDosage(updatedMedicine.getDosage());
        existing.setExpirationDate(updatedMedicine.getExpirationDate());

        return medicineRepository.save(existing);
    }


    @Transactional
    public void updateMedicinaleAvailableQuantity(Long medicinaleId, int newAvailableQuantity) {
        int updatedRows = medicineRepository.updateAvailableQuantity(medicinaleId, newAvailableQuantity);
        if (updatedRows == 0) {
            throw new RuntimeException("Nessuna riga aggiornata! Controlla se l'ID esiste.");
        }
    }

    @Transactional
    public void deleteMedicinale(Long id) {
        if (!medicineRepository.existsById(id)) {
            throw new IllegalArgumentException("Farmaco non trovato con ID: " + id);
        }
        medicineRepository.deleteById(id);
    }


    @Transactional
    public List<Medicine> getAvailableMedicines() {
        return medicineRepository.findByQuantityGreaterThan(0);
    }
}
