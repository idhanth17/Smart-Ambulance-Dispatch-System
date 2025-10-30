package com.smartambulance.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "patient")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private Double latitude;
    private Double longitude;

    // Many patients may refer to one ambulance assigned
    @ManyToOne
    @JoinColumn(name = "assigned_ambulance_id")
    private Ambulance assignedAmbulance;

    public Patient() {}

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Ambulance getAssignedAmbulance() { return assignedAmbulance; }
    public void setAssignedAmbulance(Ambulance assignedAmbulance) { this.assignedAmbulance = assignedAmbulance; }
}
