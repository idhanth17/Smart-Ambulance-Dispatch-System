package com.smartambulance.backend.repository;

import com.smartambulance.backend.model.Ambulance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {
    List<Ambulance> findByOnDutyTrueAndBusyFalse(); // available ambulances
}
