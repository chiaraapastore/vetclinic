package com.vetclinic.repository;

import com.vetclinic.models.DocumentoClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoClinicoRepository extends JpaRepository<DocumentoClinico, Long> {

    List<DocumentoClinico> findByAnimaleId(Long animalId);

    List<DocumentoClinico> findByVeterinarioId(Long veterinarioId);

    List<DocumentoClinico> findByAssistantId(Long assistenteId);
}
