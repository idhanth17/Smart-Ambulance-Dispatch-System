package com.smartambulance.backend.controller;

import com.smartambulance.backend.model.Hospital;
import com.smartambulance.backend.service.HospitalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {
    private final HospitalService service;
    public HospitalController(HospitalService service){ this.service = service; }

    @GetMapping
    public List<Hospital> all(){ return service.all(); }

    @GetMapping("/status/{status}")
    public List<Hospital> byStatus(@PathVariable String status){ return service.byStatus(status); }

    @PostMapping
    public ResponseEntity<Hospital> create(@RequestBody Hospital h){
        if (h.getStatus() == null) h.setStatus("Available");
        Hospital saved = service.save(h);
        return ResponseEntity.status(201).body(saved);
    }
}
