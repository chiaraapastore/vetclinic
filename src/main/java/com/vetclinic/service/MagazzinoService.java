package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.Magazzino;
import com.vetclinic.models.Medicine;
import com.vetclinic.repository.MagazzinoRepository;
import com.vetclinic.repository.MedicineRepository;
import com.vetclinic.models.Utente;
import com.vetclinic.repository.UtenteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class MagazzinoService {

    private final MagazzinoRepository magazzinoRepository;
    private final MedicineRepository medicineRepository;
    private final AuthenticationService authenticationService;
    private final UtenteRepository utenteRepository;

    public MagazzinoService(MagazzinoRepository magazzinoRepository, UtenteRepository utenteRepository, MedicineRepository medicineRepository, AuthenticationService authenticationService) {
        this.magazzinoRepository = magazzinoRepository;
        this.medicineRepository = medicineRepository;
        this.utenteRepository = utenteRepository;
        this.authenticationService = authenticationService;
    }



    @Transactional
    public Magazzino getStock() {
        Utente utenteAdmin = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        if (utenteAdmin == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }
        return magazzinoRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No stock found"));
    }

    @Transactional
    public Magazzino createStock(Magazzino magazzino) {
        Utente utenteAdmin = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        if (utenteAdmin == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }
        if (magazzinoRepository.count() > 0) {
            throw new IllegalStateException("Stock already exists. Cannot create another one.");
        }
        return magazzinoRepository.save(magazzino);
    }

    @Transactional
    public void updateStock(Magazzino magazzino) {
        Utente utenteAdmin = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin non trovato"));

        if (utenteAdmin == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }

        Magazzino existingStock = getStock();
        existingStock.setCurrentStock(magazzino.getCurrentStock());
        existingStock.setMaximumCapacity(magazzino.getMaximumCapacity());
        magazzinoRepository.save(existingStock);
    }

    @Transactional
    public boolean updateMedicineStock(Long medicineId, int quantity) {
        Utente utenteAdmin = utenteRepository.findByKeycloakId(authenticationService.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin non trovato"));

        if (utenteAdmin == null) {
            throw new IllegalArgumentException("Utente non autenticato");
        }
        Optional<Medicine> medicineOpt = medicineRepository.findById(medicineId);

        if (medicineOpt.isPresent()) {
            Medicine medicine = medicineOpt.get();

            if (medicine.getAvailableQuantity() >= quantity) {
                medicine.setAvailableQuantity(medicine.getAvailableQuantity() - quantity);
                medicineRepository.save(medicine);
                return true;
            } else {
                return false;
            }
        } else {
            throw new RuntimeException("Medicine with ID " + medicineId + " not found.");
        }
    }

    @Transactional
    public void checkAndUpdateMedicineStock(Long medicineId, int quantity) {
        Optional<Medicine> medicineOpt = medicineRepository.findById(medicineId);

        if (medicineOpt.isEmpty()) {
            throw new RuntimeException("Medicinale con ID " + medicineId + " non trovato.");
        }

        Medicine medicine = medicineOpt.get();

        if (isMedicineOutOfStock(medicine)) {
            System.out.println("Il farmaco " + medicine.getName() + " è esaurito!");
        } else {
            medicine.setAvailableQuantity(medicine.getAvailableQuantity() - quantity);
            medicineRepository.save(medicine);
            System.out.println("Quantità aggiornata per " + medicine.getName());
        }
    }

    private boolean isMedicineOutOfStock(Medicine medicine) {
        return calculateAvailableStock(medicine) <= 0;
    }

    private int calculateAvailableStock(Medicine medicine) {
        return medicine.getCurrentStock() - medicine.getPendingOrders() + medicine.getUnitsToReceive();
    }
}
