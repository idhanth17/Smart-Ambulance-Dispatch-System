package com.smartambulance.backend.controller;

import com.smartambulance.backend.model.Patient;
import com.smartambulance.backend.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService service;
    public PatientController(PatientService service){ this.service = service; }

    @GetMapping
    public List<Patient> all(){ return service.all(); }

    @GetMapping("/unassigned")
    public List<Patient> unassigned(){ return service.unassigned(); }

    @PostMapping
    public ResponseEntity<Patient> create(@RequestBody Patient p){
        Patient saved = service.save(p);
        return ResponseEntity.status(201).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
