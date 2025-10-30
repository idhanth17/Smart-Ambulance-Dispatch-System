package com.smartambulance.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ambulance")
public class Ambulance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String driverName;
    private String vehicleNumber;
    private String location; // textual location
    private Double latitude;
    private Double longitude;

    // we keep onDuty and busy and they are NOT NULL with default values
    @Column(nullable = false)
    private boolean onDuty = false;

    @Column(nullable = false)
    private boolean busy = false;

    public Ambulance() {}

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public boolean isOnDuty() { return onDuty; }
    public void setOnDuty(boolean onDuty) { this.onDuty = onDuty; }

    public boolean isBusy() { return busy; }
    public void setBusy(boolean busy) { this.busy = busy; }
}
