package com.smartambulance.frontend.controllers;

import com.smartambulance.frontend.models.Ambulance;
import com.smartambulance.frontend.models.Patient;
import com.smartambulance.frontend.models.Hospital;
import com.smartambulance.frontend.util.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.util.List;

public class AmbulanceDetailController {
    @FXML private Label titleLabel;
    @FXML private WebView detailMap;
    @FXML private Button dutyButton;
    @FXML private Button pickupButton;
    @FXML private Button dropButton;
    @FXML private Label infoLabel;

    private Ambulance ambulance;
    private WebEngine webEngine;
    private final String mapHtml = getMapHtml();

    public void setAmbulance(Ambulance a) {
        this.ambulance = a;
        titleLabel.setText("Ambulance: " + (a.getDriverName()!=null?a.getDriverName():"#" + a.getId()));
        initUI();
        refreshState();
    }

    private void initUI(){
        webEngine = detailMap.getEngine();
        webEngine.loadContent(mapHtml);
    }

    private String getMapHtml(){
        return """
        <!DOCTYPE html><html><head><meta charset="utf-8" /><meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>html,body,#map{height:100%;width:100%;margin:0;padding:0}#map{height:400px;}</style>
        </head><body><div id="map"></div><script>
        var map = L.map('map').setView([11.0168,76.9558],13);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19}).addTo(map);
        function addMarker(lat, lon, label, color){ var icon = L.icon({ iconUrl: 'https://chart.googleapis.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|'+color, iconSize:[21,34], iconAnchor:[10,34], popupAnchor:[1,-34]}); L.marker([lat,lon],{icon:icon}).addTo(map).bindPopup(label); }
        window.showAmbulance = function(lat, lon, name){ addMarker(lat, lon, '🚑 '+name, '0000FF'); map.setView([lat,lon],14);};
        window.showPatient = function(lat, lon, name){ addMarker(lat, lon, '🧍 '+name, 'FF0000'); map.setView([lat,lon],14);};
        window.clearMap = function(){ map.eachLayer(function(l){ if (l instanceof L.Marker) map.removeLayer(l); }); }
        </script></body></html>
        """;
    }

    private void refreshState(){
        Platform.runLater(() -> {
            try {
                // reload ambulance from backend
                ambulance = ApiClient.getAmbulances().stream().filter(a -> a.getId().equals(ambulance.getId())).findFirst().orElse(ambulance);
                updateButtons();
                render();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateButtons(){
        dutyButton.setDisable(false);
        // if assigned (busy) disable duty toggle and show pickup/drop flow
        if (ambulance.isBusy()) {
            dutyButton.setDisable(true);
            pickupButton.setDisable(true);
            dropButton.setDisable(false);
            infoLabel.setText("Assigned to patient — use Drop to finish.");
        } else {
            // if no assigned patient => allow duty toggle and pickup disabled
            dutyButton.setText(ambulance.isOnDuty() ? "Set Off Duty" : "Set On Duty");
            pickupButton.setDisable(false);
            dropButton.setDisable(true);
            infoLabel.setText("No assigned patient.");
        }
    }

    private void render(){
        webEngine.executeScript("window.clearMap();");
        if (ambulance.getLatitude()!=null && ambulance.getLongitude()!=null) {
            String js = String.format("window.showAmbulance(%s,%s,%s);",
                    ambulance.getLatitude(), ambulance.getLongitude(), quoteJs(ambulance.getDriverName()));
            webEngine.executeScript(js);
        }
        // if busy, load assigned patient location
        try {
            List<Patient> patients = ApiClient.getPatients();
            for (Patient p : patients) {
                if (p.getAssignedAmbulance()!=null && p.getAssignedAmbulance().getId().equals(ambulance.getId())) {
                    if (p.getLatitude()!=null && p.getLongitude()!=null) {
                        String js = String.format("window.showPatient(%s,%s,%s);",
                                p.getLatitude(), p.getLongitude(), quoteJs(p.getName()));
                        webEngine.executeScript(js);
                    }
                    infoLabel.setText("Assigned patient: " + p.getName());
                    return;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void onToggleDuty(){
        try {
            ambulance.setOnDuty(!ambulance.isOnDuty());
            ApiClient.sendPut("/ambulances/" + ambulance.getId(), ambulance); // helper method you'll add below or call update endpoint
            refreshState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onPickup(){
        // In this simplified design pickup is just marking ambulance busy and associating the first unassigned patient nearby
        try {
            // find unassigned patient (or fail)
            List<Patient> unassigned = ApiClient.getPatients();
            Patient pick = null;
            for (Patient p : unassigned) {
                if (p.getAssignedAmbulance()==null) { pick = p; break; }
            }
            if (pick == null) {
                infoLabel.setText("No unassigned patient available.");
                return;
            }
            boolean ok = ApiClient.assignAmbulance(pick.getId(), ambulance.getId());
            if (ok) {
                refreshState();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void onDrop(){
        try {
            // show hospitals and pick first (for demo) or call endpoint to confirm with supplied hospital id - we'll fetch list and pick first
            List<Hospital> hospitals = ApiClient.getHospitals();
            Long hid = hospitals.isEmpty() ? null : hospitals.get(0).getId();
            boolean ok = ApiClient.confirmDrop(ambulance.getId(), hid == null ? -1L : hid);
            if (ok) {
                refreshState();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // utility to call PUT because ApiClient had private sendPut; we'll reuse it by reflection OR add public method.
    // For now use a small wrapper: ApiClient has public sendPut(...) in earlier code — if not, add it.
    private String quoteJs(String s){ if (s==null) return "\"\""; return "\"" + s.replace("\"","\\\"") + "\""; }
}
