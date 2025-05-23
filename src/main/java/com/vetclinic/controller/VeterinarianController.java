package com.vetclinic.controller;


import com.vetclinic.models.*;
import com.vetclinic.repository.AnimaleRepository;
import com.vetclinic.repository.MedicineRepository;
import com.vetclinic.service.CapoRepartoService;
import com.vetclinic.service.VeterinarianService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/veterinarian")
public class VeterinarianController {


    private final AnimaleRepository animaleRepository;
    private final MedicineRepository medicineRepository;
    private final VeterinarianService veterinarianService;
    private final CapoRepartoService capoRepartoService ;

    public VeterinarianController(VeterinarianService veterinarianService, AnimaleRepository animaleRepository, MedicineRepository medicineRepository,  CapoRepartoService capoRepartoService) {
        this.veterinarianService = veterinarianService;
        this.animaleRepository = animaleRepository;
        this.medicineRepository = medicineRepository;
        this.capoRepartoService = capoRepartoService;

    }

    @PostMapping("/execute-operation")
    public ResponseEntity<Map<String, String>> executeOperation(@RequestParam Long animaleId,
                                                                @RequestParam String tipoOperazione,
                                                                @RequestParam String descrizioneOperazione) {
        String result = veterinarianService.executeOperation(animaleId, tipoOperazione, descrizioneOperazione);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @GetMapping("/view-medicine/{departmentId}")
    public ResponseEntity<List<Medicine>> getMedicineByDepartment(@PathVariable Long departmentId) {
        List<Medicine> medicinali = veterinarianService.viewMedicineDepartment(departmentId);
        return ResponseEntity.ok(medicinali);
    }


    @PostMapping("/administers-medicines")
    public ResponseEntity<Map<String, String>> administersMedicines(
            @RequestParam Long pazienteId,
            @RequestParam Long medicineId,
            @RequestParam int quantita,
            @RequestParam Long capoRepartoId){
        String result = veterinarianService.administersMedicines(pazienteId, capoRepartoId,  medicineId, quantita);
        return ResponseEntity.ok(Map.of("message", result));
    }




    @PostMapping("/expiration")
    public ResponseEntity<?> expirationMedicine( @PathVariable Long headOfDepartmentId, @PathVariable Long medicineId) {
        veterinarianService.expirationMedicine(headOfDepartmentId, medicineId);
        return ResponseEntity.ok("notifica_inviata");
    }


    @GetMapping("/animals")
    public ResponseEntity<List<Animale>> getVeterinarianPatients() {
        List<Animale> patients = veterinarianService.getAnimalsOfVeterinarian();
        return ResponseEntity.ok(patients);
    }



    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Veterinario>> getVeterinariesByDepartment(@PathVariable Long departmentId) {

        List<Veterinario> dottori = veterinarianService.getVeterinariesByDepartment(departmentId);

        if (dottori.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(dottori);
    }


    @GetMapping("/{emailVeterinarian}/department")
    public ResponseEntity<Reparto> getDepartmentByVeterinarian(@PathVariable String emailVeterinarian) {
        Reparto reparto = veterinarianService.getDepartmentByVeterinarian(emailVeterinarian);
        return ResponseEntity.ok(reparto);
    }

    @PutMapping("/change-department/{veterinarianId}/{newDepartmentId}")
    public ResponseEntity<String> changeDepartment(@PathVariable Long veterinarianId, @PathVariable Long newDepartmentId) {
        capoRepartoService.changeDepartment(veterinarianId, newDepartmentId);
        return ResponseEntity.ok("Reparto cambiato con successo");
    }


}
