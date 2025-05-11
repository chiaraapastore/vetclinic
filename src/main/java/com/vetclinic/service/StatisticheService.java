package com.vetclinic.service;

import com.vetclinic.models.Medicine;
import com.vetclinic.models.Somministrazione;
import com.vetclinic.repository.MedicineRepository;
import com.vetclinic.repository.SomministrazioneRepository;
import com.vetclinic.repository.RepartoRepository;
import com.vetclinic.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticheService {

    private final SomministrazioneRepository somministrazioneRepository;
    private final MedicineRepository medicineRepository;
    private final RepartoRepository repartoRepository;
    private final UtenteRepository utenteRepository;

    public StatisticheService(SomministrazioneRepository somministrazioneRepository, MedicineRepository medicineRepository, RepartoRepository repartoRepository, UtenteRepository utenteRepository) {
        this.somministrazioneRepository = somministrazioneRepository;
        this.medicineRepository = medicineRepository;
        this.repartoRepository = repartoRepository;
        this.utenteRepository = utenteRepository;
    }

    @Transactional
    public Map<String, Object> getConsumiNelTempo() {
        List<Somministrazione> somministrazioni = somministrazioneRepository.findAll();


        Map<String, Integer> consumiPerData = somministrazioni.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getDate().toLocalDate().toString(),
                        Collectors.summingInt(Somministrazione::getDosage)
                ));

        List<Map<String, Object>> series = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : consumiPerData.entrySet()) {
            series.add(Map.of("name", entry.getKey(), "value", entry.getValue()));
        }

        return Map.of("name", "Farmaci Somministrati", "series", series);
    }

    @Transactional
    public Map<String, Object> getRiordiniStockout() {
        long totaleRiordini = medicineRepository.count();
        long stockout = medicineRepository.findByAvailableQuantityLessThanEqual(0).size();

        return Map.of(
                "riordini", Map.of("name", "Riordini", "value", totaleRiordini),
                "stockout", Map.of("name", "Stockout", "value", stockout)
        );
    }

    @Transactional
    public List<Map<String, Object>> getDistribuzionePerReparto() {
        List<Map<String, Object>> risultato = medicineRepository.findConsumoPerReparto();


        return risultato;
    }
}
