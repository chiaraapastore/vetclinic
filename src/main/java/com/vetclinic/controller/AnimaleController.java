package com.vetclinic.controller;

import com.vetclinic.models.Animale;
import com.vetclinic.service.AnimaleService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/animali")
public class AnimaleController {

    private final AnimaleService animaleService;

    public AnimaleController(AnimaleService animaleService) {
        this.animaleService = animaleService;
    }


    @GetMapping("/cliente")
    public ResponseEntity<List<Animale>> getAnimalsOfClient() {
        List<Animale> animali = animaleService.getAnimalsOfClient();
        return ResponseEntity.ok(animali);
    }


    @GetMapping("/{animaleId}")
    public ResponseEntity<Animale> getAnimaleById(@PathVariable Long animaleId) {
        Animale animale = animaleService.getAnimaleById(animaleId);
        return ResponseEntity.ok(animale);
    }


    @PostMapping("/add")
    public ResponseEntity<Animale> addAnimale(@RequestBody Animale animale) {
        Animale newAnimale = animaleService.addAnimale(animale);
        return ResponseEntity.ok(newAnimale);
    }


    @PutMapping("/update/{animaleId}")
    public ResponseEntity<Animale> updateAnimale(@PathVariable Long animaleId, @RequestBody Animale animaleDetails) {
        Animale updatedAnimale = animaleService.updateAnimale(animaleId, animaleDetails);
        return ResponseEntity.ok(updatedAnimale);
    }

    @DeleteMapping("/delete/{animaleId}")
    public ResponseEntity<String> deleteAnimale(@PathVariable Long animaleId) {
        animaleService.deleteAnimale(animaleId);
        return ResponseEntity.ok("Animale eliminato con successo");
    }

    @GetMapping("/download-cartella-clinica/{animaleId}")
    public ResponseEntity<byte[]> downloadMedicalRecord(@PathVariable Long animaleId) {
        try {
            byte[] pdfBytes = animaleService.generateCartellaClinicaPdf(animaleId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cartella_clinica.pdf")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
