package com.smartambulance.frontend.controllers;

import com.smartambulance.frontend.models.Ambulance;
import com.smartambulance.frontend.models.Patient;
import com.smartambulance.frontend.util.ApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.util.List;

public class ControllerTabController {
    @FXML private ListView<Ambulance> ambulanceList;
    @FXML private ListView<Patient> patientList;
    @FXML private WebView mapView;

    private WebEngine webEngine;
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        webEngine = mapView.getEngine();
        webEngine.loadContent(getMapHtml());

        // Wait until the HTML/JS is fully loaded before refreshing data
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                // Now JS functions like window.clearMap() are available
                Platform.runLater(this::refreshAll);
            }
        });
    }

    private String getMapHtml() {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map {
                    height: 100%;
                    width: 100%;
                    margin: 0;
                    padding: 0;
                }
                #map { height: 100vh; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map').setView([11.0168, 76.9558], 13);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19
                }).addTo(map);

                function addMarker(lat, lon, label, color) {
                    var icon = L.icon({
                        iconUrl: 'https://chart.googleapis.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|' + color,
                        iconSize: [21, 34],
                        iconAnchor: [10, 34],
                        popupAnchor: [1, -34]
                    });
                    L.marker([lat, lon], { icon: icon }).addTo(map).bindPopup(label);
                }

                window.showAmbulance = function(lat, lon, name) {
                    addMarker(lat, lon, '🚑 ' + name, '0000FF');
                };
                window.showPatient = function(lat, lon, name) {
                    addMarker(lat, lon, '🧍 ' + name, 'FF0000');
                };
                window.clearMap = function() {
                    map.eachLayer(function(layer) {
                        if (layer instanceof L.Marker) {
                            map.removeLayer(layer);
                        }
                    });
                };
            </script>
        </body>
        </html>
        """;
    }

    @FXML
    public void onRefresh() {
        refreshAll();
    }

    private void refreshAll() {
        try {
            if (webEngine.getLoadWorker().getState() != Worker.State.SUCCEEDED) {
                // JS not ready yet, try again after small delay
                Platform.runLater(() -> refreshAll());
                return;
            }

            List<Ambulance> ambulances = ApiClient.getAmbulances();
            List<Patient> patients = ApiClient.getPatients();

            ambulanceList.getItems().setAll(ambulances);
            patientList.getItems().setAll(patients);

            webEngine.executeScript("window.clearMap();");

            // Add ambulances
            for (Ambulance a : ambulances) {
                if (a.getLatitude() != null && a.getLongitude() != null) {
                    String js = String.format("window.showAmbulance(%s, %s, %s);",
                            a.getLatitude(), a.getLongitude(), quoteJs(a.getDriverName()));
                    webEngine.executeScript(js);
                }
            }

            // Add patients
            for (Patient p : patients) {
                if (p.getLatitude() != null && p.getLongitude() != null) {
                    String js = String.format("window.showPatient(%s, %s, %s);",
                            p.getLatitude(), p.getLongitude(), quoteJs(p.getName()));
                    webEngine.executeScript(js);
                }
            }

        } catch (Exception ex) {
            showError("Failed to refresh", ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    public void onAssign() {
        Ambulance a = ambulanceList.getSelectionModel().getSelectedItem();
        Patient p = patientList.getSelectionModel().getSelectedItem();

        if (a == null || p == null) {
            showError("Select both", "Select an ambulance and a patient to assign.");
            return;
        }

        try {
            boolean ok = ApiClient.assignAmbulance(p.getId(), a.getId());
            if (ok) {
                refreshAll();
                showInfo("Assigned", "Ambulance assigned to patient.");
            } else {
                showError("Assign failed", "Server rejected assignment.");
            }
        } catch (Exception e) {
            showError("Assign failed", e.getMessage());
            e.printStackTrace();
        }
    }

    private String quoteJs(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
