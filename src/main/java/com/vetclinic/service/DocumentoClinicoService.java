package com.vetclinic.service;

import com.vetclinic.models.*;
import com.vetclinic.repository.DocumentoClinicoRepository;
import com.vetclinic.repository.AnimaleRepository;
import com.vetclinic.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocumentoClinicoService {

    private final DocumentoClinicoRepository documentoClinicoRepository;
    private final AnimaleRepository animaleRepository;
    private final UtenteRepository utenteRepository;

    public DocumentoClinicoService(DocumentoClinicoRepository documentoClinicoRepository, AnimaleRepository animaleRepository, UtenteRepository utenteRepository, AnimaleService animaleService) {
        this.documentoClinicoRepository = documentoClinicoRepository;
        this.animaleRepository = animaleRepository;
        this.utenteRepository = utenteRepository;
    }

    @Transactional
    public DocumentoClinico addDocumentToAnimal(Long animalId, Long veterinarianId, Long assistantId, String documentType, String documentName, String documentPath) {
        Animale animal = animaleRepository.findById(animalId)
                .orElseThrow(() -> new IllegalArgumentException("Animale non trovato"));

        VeterinarioDTO veterinarian = (VeterinarioDTO) utenteRepository.findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinario non trovato"));

        Assistente assistant = (Assistente) utenteRepository.findById(assistantId)
                .orElseThrow(() -> new IllegalArgumentException("Assistente non trovato"));

        DocumentoClinico document = new DocumentoClinico();
        document.setAnimale(animal);
        document.setVeterinario(veterinarian);
        document.setAssistant(assistant);
        document.setDocumentType(documentType);
        document.setDocumentName(documentName);
        document.setDocumentPath(documentPath);

        return documentoClinicoRepository.save(document);
    }

    @Transactional
    public List<DocumentoClinico> getDocumentsByAnimal(Long animalId) {
        return documentoClinicoRepository.findByAnimaleId(animalId);
    }

    @Transactional
    public List<DocumentoClinico> getDocumentsByVeterinarian(Long veterinarianId) {
        return documentoClinicoRepository.findByVeterinarioId(veterinarianId);
    }

    @Transactional
    public List<DocumentoClinico> getDocumentsByAssistant(Long assistantId) {
        return documentoClinicoRepository.findByAssistantId(assistantId);
    }



}
