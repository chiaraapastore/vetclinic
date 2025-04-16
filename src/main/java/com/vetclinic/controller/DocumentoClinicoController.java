package com.vetclinic.controller;

import com.itextpdf.text.DocumentException;
import com.vetclinic.models.DocumentoClinico;
import com.vetclinic.service.AnimaleService;
import com.vetclinic.service.DocumentoClinicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documenti-clinici")
public class DocumentoClinicoController {

    private final DocumentoClinicoService documentoClinicoService;
    private final AnimaleService animaleService;

    public DocumentoClinicoController(DocumentoClinicoService documentoClinicoService, AnimaleService animaleService) {
        this.documentoClinicoService = documentoClinicoService;
        this.animaleService = animaleService;
    }

    @PostMapping("/add")
    public ResponseEntity<DocumentoClinico> addDocumentToAnimal(
            @RequestParam Long animalId,
            @RequestParam Long veterinarianId,
            @RequestParam Long assistantId,
            @RequestParam String documentType,
            @RequestParam String documentName,
            @RequestParam String documentPath) {

        DocumentoClinico newDocument = documentoClinicoService.addDocumentToAnimal(
                animalId, veterinarianId, assistantId, documentType, documentName, documentPath);
        return ResponseEntity.ok(newDocument);
    }

    @GetMapping("/animal/{animalId}")
    public ResponseEntity<List<DocumentoClinico>> getDocumentsByAnimal(@PathVariable Long animalId) {
        List<DocumentoClinico> documents = documentoClinicoService.getDocumentsByAnimal(animalId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/veterinarian/{veterinarianId}")
    public ResponseEntity<List<DocumentoClinico>> getDocumentsByVeterinarian(@PathVariable Long veterinarianId) {
        List<DocumentoClinico> documents = documentoClinicoService.getDocumentsByVeterinarian(veterinarianId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/assistant/{assistantId}")
    public ResponseEntity<List<DocumentoClinico>> getDocumentsByAssistant(@PathVariable Long assistantId) {
        List<DocumentoClinico> documents = documentoClinicoService.getDocumentsByAssistant(assistantId);
        return ResponseEntity.ok(documents);
    }


    @GetMapping("/{clienteId}/{animaleId}/download")
    public ResponseEntity<byte[]> downloadDocumentAndInvoicePdf(
            @PathVariable Long clienteId, @PathVariable Long animaleId) throws DocumentException {

        byte[] pdfBytes = animaleService.generateDocumentoClinicoAndFatturaPdf(animaleId);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=documento_clinico_e_fattura.pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
