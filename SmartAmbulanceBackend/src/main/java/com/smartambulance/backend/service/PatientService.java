package com.smartambulance.backend.service;

import com.smartambulance.backend.model.Patient;
import com.smartambulance.backend.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {
    private final PatientRepository repo;
    public PatientService(PatientRepository repo) { this.repo = repo; }
    public Patient save(Patient p){ return repo.save(p); }
    public List<Patient> all(){ return repo.findAll(); }
    public Optional<Patient> find(Long id){ return repo.findById(id); }
    public void delete(Long id){ repo.deleteById(id); }
    public List<Patient> unassigned(){ return repo.findByAssignedAmbulanceIsNull(); }
}
