package com.smartambulance.backend.controller;

import com.smartambulance.backend.model.Ambulance;
import com.smartambulance.backend.service.AmbulanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ambulances")
public class AmbulanceController {
    private final AmbulanceService service;
    public AmbulanceController(AmbulanceService service){ this.service = service; }

    @GetMapping
    public List<Ambulance> all(){ return service.all(); }

    @GetMapping("/available")
    public List<Ambulance> available(){ return service.available(); }

    @PostMapping
    public ResponseEntity<Ambulance> register(@RequestBody Ambulance ambul){
        // Ensure booleans non-null
        if (ambul.getLatitude() == null) ambul.setLatitude(0.0);
        if (ambul.getLongitude() == null) ambul.setLongitude(0.0);
        Ambulance saved = service.save(ambul);
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ambulance> update(@PathVariable Long id, @RequestBody Ambulance update){
        return service.find(id).map(existing -> {
            // update fields
            existing.setDriverName(update.getDriverName());
            existing.setVehicleNumber(update.getVehicleNumber());
            existing.setLocation(update.getLocation());
            existing.setLatitude(update.getLatitude());
            existing.setLongitude(update.getLongitude());
            existing.setOnDuty(update.isOnDuty());
            existing.setBusy(update.isBusy());
            return ResponseEntity.ok(service.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }
}
