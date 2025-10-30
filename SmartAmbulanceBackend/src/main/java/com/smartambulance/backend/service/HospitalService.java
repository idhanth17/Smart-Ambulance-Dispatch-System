package com.smartambulance.backend.service;

import com.smartambulance.backend.model.Hospital;
import com.smartambulance.backend.repository.HospitalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HospitalService {
    private final HospitalRepository repo;
    public HospitalService(HospitalRepository repo) { this.repo = repo; }
    public Hospital save(Hospital h){ return repo.save(h); }
    public List<Hospital> all(){ return repo.findAll(); }
    public List<Hospital> byStatus(String s){ return repo.findByStatus(s); }
    public Optional<Hospital> find(Long id){ return repo.findById(id); }
}
