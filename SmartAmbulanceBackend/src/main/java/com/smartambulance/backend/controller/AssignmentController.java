package com.smartambulance.backend.controller;

import com.smartambulance.backend.model.Ambulance;
import com.smartambulance.backend.model.Patient;
import com.smartambulance.backend.service.AmbulanceService;
import com.smartambulance.backend.service.PatientService;
import com.smartambulance.backend.service.HospitalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AssignmentController {
    private final AmbulanceService ambulanceService;
    private final PatientService patientService;
    private final HospitalService hospitalService;

    public AssignmentController(AmbulanceService ambulanceService, PatientService patientService, HospitalService hospitalService) {
        this.ambulanceService = ambulanceService;
        this.patientService = patientService;
        this.hospitalService = hospitalService;
    }

    // payload: {"patientId":1,"ambulanceId":2}
    @PostMapping("/assign")
    public ResponseEntity<?> assign(@RequestBody Map<String, Long> body) {
        Long pid = body.get("patientId");
        Long aid = body.get("ambulanceId");
        if (pid == null || aid == null) return ResponseEntity.badRequest().body(Map.of("error","patientId and ambulanceId required"));
        var pOpt = patientService.find(pid);
        var aOpt = ambulanceService.find(aid);
        if (pOpt.isEmpty() || aOpt.isEmpty()) return ResponseEntity.notFound().build();

        Patient p = pOpt.get();
        Ambulance a = aOpt.get();

        if (!a.isOnDuty() || a.isBusy()) {
            return ResponseEntity.badRequest().body(Map.of("error","ambulance not available"));
        }

        a.setBusy(true);
        p.setAssignedAmbulance(a);

        ambulanceService.save(a);
        patientService.save(p);

        return ResponseEntity.ok(Map.of("success",true));
    }

    // payload: {"ambulanceId":2,"hospitalId":3}
    @PostMapping("/drop")
    public ResponseEntity<?> drop(@RequestBody Map<String, Long> body) {
        Long aid = body.get("ambulanceId");
        Long hid = body.get("hospitalId"); // for now we don't use hid except maybe logging
        var aOpt = ambulanceService.find(aid);
        if (aOpt.isEmpty()) return ResponseEntity.notFound().build();
        Ambulance a = aOpt.get();

        // find patient assigned to this ambulance
        var allPatients = patientService.all();
        Patient patientToRemove = null;
        for (Patient p : allPatients) {
            if (p.getAssignedAmbulance() != null && p.getAssignedAmbulance().getId().equals(a.getId())) {
                patientToRemove = p;
                break;
            }
        }
        if (patientToRemove == null) {
            // nothing assigned
            a.setBusy(false);
            ambulanceService.save(a);
            return ResponseEntity.ok(Map.of("success", true, "message", "no patient assigned"));
        }

        // unassign and delete patient (as you requested)
        patientService.delete(patientToRemove.getId());
        a.setBusy(false);
        ambulanceService.save(a);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
