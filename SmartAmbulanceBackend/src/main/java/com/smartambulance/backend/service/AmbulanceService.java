package com.smartambulance.backend.service;

import com.smartambulance.backend.model.Ambulance;
import com.smartambulance.backend.repository.AmbulanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AmbulanceService {
    private final AmbulanceRepository repo;

    public AmbulanceService(AmbulanceRepository repo) { this.repo = repo; }

    public Ambulance save(Ambulance a) { return repo.save(a); }
    public List<Ambulance> all() { return repo.findAll(); }
    public Optional<Ambulance> find(Long id) { return repo.findById(id); }
    public List<Ambulance> available() { return repo.findByOnDutyTrueAndBusyFalse(); }
}
