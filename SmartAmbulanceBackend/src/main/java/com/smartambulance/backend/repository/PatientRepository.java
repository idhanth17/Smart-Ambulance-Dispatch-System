package com.smartambulance.backend.repository;

import com.smartambulance.backend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByAssignedAmbulanceIsNull();
}
