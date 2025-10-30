package com.smartambulance.frontend.models;

public class Ambulance {
    private Long id;
    private String driverName;
    private String vehicleNumber;
    private String contactNumber;
    private String location;   // ✅ added back
    private String status;     // e.g. "Available", "On Duty"
    private Double latitude;
    private Double longitude;
    private boolean available; // used internally for UI logic

    public Ambulance() {}

    // ---------- Basic Getters & Setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getLocation() { return location; }     // ✅ Added
    public void setLocation(String location) { this.location = location; }  // ✅ Added

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.available = "Available".equalsIgnoreCase(status);
    }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    // ---------- Logic Helpers ----------
    public boolean isAvailable() {
        if (status != null) {
            return "Available".equalsIgnoreCase(status);
        }
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
        this.status = available ? "Available" : "On Duty";
    }

    public boolean isBusy() {
        return !isAvailable();
    }

    public void setBusy(boolean busy) {
        setAvailable(!busy);
    }

    public boolean isOnDuty() {
        return "On Duty".equalsIgnoreCase(status);
    }

    public void setOnDuty(boolean onDuty) {
        this.status = onDuty ? "On Duty" : "Available";
        this.available = !onDuty;
    }

    @Override
    public String toString() {
        return driverName + " (" + vehicleNumber + ")";
    }
}
