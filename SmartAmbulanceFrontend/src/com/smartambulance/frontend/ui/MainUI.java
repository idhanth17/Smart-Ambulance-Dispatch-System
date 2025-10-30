package com.smartambulance.frontend.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartambulance.frontend.models.Ambulance;
import com.smartambulance.frontend.models.Hospital;
import com.smartambulance.frontend.models.Patient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainUI {

    private BorderPane root;
    private TabPane tabPane;

    // REST base
    private final String BASE = "http://localhost:8080/api";
    private final Gson gson = new Gson();

    // UI components for controller tab
    private WebView mapView;
    private WebEngine webEngine;
    private ObservableList<Ambulance> ambulances = FXCollections.observableArrayList();
    private ObservableList<Patient> patients = FXCollections.observableArrayList();
    private ObservableList<Hospital> hospitals = FXCollections.observableArrayList();

    private ListView<Patient> patientListView;
    private ComboBox<Ambulance> ambulanceAssignBox;
    private Button assignButton;
    private Button refreshButton;

    // ambulance tab
    private ComboBox<Ambulance> ambulanceSelector;
    private Button dutyToggleButton;
    private TextField driverNameField, vehicleField, locationField, latField, lonField;
    private Button registerButton;

    public MainUI() {
        root = new BorderPane();
        tabPane = new TabPane();
        Tab controllerTab = new Tab("Controller");
        controllerTab.setContent(createControllerTab());
        controllerTab.setClosable(false);

        Tab driverTab = new Tab("Ambulance");
        driverTab.setContent(createAmbulanceTab());
        driverTab.setClosable(false);

        tabPane.getTabs().addAll(controllerTab, driverTab);
        root.setCenter(tabPane);

        initMap();      // load the leaflet map
        loadAllData();  // initial load
    }

    public BorderPane getRoot() { return root; }

    // === Controller tab UI ===
    private BorderPane createControllerTab() {
        BorderPane pane = new BorderPane();
        HBox topControls = new HBox(8);
        topControls.setPadding(new Insets(8));
        refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadAllData());
        topControls.getChildren().add(refreshButton);
        pane.setTop(topControls);

        // left side: patient list and assign controls
        VBox left = new VBox(8);
        left.setPadding(new Insets(8));
        left.setPrefWidth(320);

        patientListView = new ListView<>(patients);
        patientListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Patient p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) setText(null);
                else {
                    String assigned = (p.getAssignedAmbulance() == null) ? " (unassigned)" : " (assigned: " + p.getAssignedAmbulance().getDriverName() + ")";
                    setText(p.getName() + assigned + "\n" + p.getLocation());
                }
            }
        });
        left.getChildren().add(new Label("Patients:"));
        left.getChildren().add(patientListView);

        ambulanceAssignBox = new ComboBox<>(ambulances);
        ambulanceAssignBox.setPrefWidth(280);
        ambulanceAssignBox.setPromptText("Select ambulance to assign");
        assignButton = new Button("Assign Selected Ambulance to Patient");
        assignButton.setOnAction(e -> assignSelectedAmbulanceToPatient());
        left.getChildren().addAll(new Label("Available ambulances:"), ambulanceAssignBox, assignButton);

        pane.setLeft(left);

        // right side: map
        mapView = new WebView();
        mapView.setPrefSize(800, 600);
        pane.setCenter(mapView);

        return pane;
    }

    // === Ambulance tab UI ===
    private VBox createAmbulanceTab() {
        VBox v = new VBox(10);
        v.setPadding(new Insets(12));

        // existing ambulances
        HBox top = new HBox(8);
        ambulanceSelector = new ComboBox<>(ambulances);
        ambulanceSelector.setPrefWidth(300);
        ambulanceSelector.setPromptText("Select existing ambulance");
        ambulanceSelector.setOnAction(e -> onAmbulanceSelected());
        top.getChildren().addAll(new Label("Ambulance:"), ambulanceSelector);

        dutyToggleButton = new Button("Toggle Duty");
        dutyToggleButton.setOnAction(e -> toggleDuty());
        top.getChildren().add(dutyToggleButton);

        v.getChildren().add(top);

        // show assigned patient info (simple)
        Label assignedLabel = new Label("Assigned Patient: none");
        v.getChildren().add(assignedLabel);

        // registration form
        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(6);
        form.setPadding(new Insets(8));
        driverNameField = new TextField();
        vehicleField = new TextField();
        locationField = new TextField();
        latField = new TextField();
        lonField = new TextField();
        registerButton = new Button("Register New Ambulance");
        registerButton.setOnAction(e -> {
            registerAmbulance();
            loadAllData();
        });

        form.addRow(0, new Label("Driver name:"), driverNameField);
        form.addRow(1, new Label("Vehicle number:"), vehicleField);
        form.addRow(2, new Label("Location text:"), locationField);
        form.addRow(3, new Label("Latitude:"), latField);
        form.addRow(4, new Label("Longitude:"), lonField);
        form.add(registerButton, 1, 5);

        v.getChildren().add(new Label("Register new driver:"));
        v.getChildren().add(form);

        return v;
    }

    // ========== Map initialization (Leaflet) ==========
    private void initMap() {
        webEngine = mapView.getEngine();
        String html = buildLeafletHtml();
        webEngine.loadContent(html);
    }

    // create a small page that exposes JS functions for adding/removing markers
    private String buildLeafletHtml() {
        return """
                <!doctype html>
                <html>
                <head>
                  <meta charset="utf-8" />
                  <title>Map</title>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
                </head>
                <body>
                <div id="map" style="width:100%; height:100%;"></div>
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <script>
                  var map = L.map('map').setView([11.0168, 76.9558], 12);
                  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19
                  }).addTo(map);
                  var markers = [];

                  function clearMarkers() {
                    markers.forEach(m => map.removeLayer(m));
                    markers = [];
                  }

                  function addMarker(lat, lon, title, color) {
                    var marker = L.circleMarker([lat, lon], {radius:8}).addTo(map).bindPopup(title);
                    markers.push(marker);
                  }

                  function setView(lat,lon,zoom) {
                    map.setView([lat,lon], zoom || 12);
                  }
                </script>
                </body>
                </html>
                """;
    }

    // call from Java to JS: add all markers
    private void refreshMapMarkers() {
        Platform.runLater(() -> {
            try {
                webEngine.executeScript("clearMarkers()");
                // add patients (blue)
                for (Patient p : patients) {
                    if (p.getLatitude() != null && p.getLongitude() != null) {
                        String title = "Patient: " + p.getName() + " (" + p.getLocation() + ")";
                        webEngine.executeScript(String.format("addMarker(%f, %f, %s, 'blue')",
                                p.getLatitude(), p.getLongitude(), toJsString(title)));
                    }
                }
                // add hospitals (green)
                for (Hospital h : hospitals) {
                    if (h.getLatitude() != null && h.getLongitude() != null) {
                        String title = "Hospital: " + h.getName() + " (" + h.getLocation() + ")";
                        webEngine.executeScript(String.format("addMarker(%f, %f, %s, 'green')",
                                h.getLatitude(), h.getLongitude(), toJsString(title)));
                    }
                }
                // add ambulances (red)
                for (Ambulance a : ambulances) {
                    if (a.getLatitude() != null && a.getLongitude() != null) {
                        String title = "Ambulance: " + (a.getDriverName() == null ? "?" : a.getDriverName())
                                + " " + (a.isAvailable() ? "available" : "busy");
                        webEngine.executeScript(String.format("addMarker(%f, %f, %s, 'red')",
                                a.getLatitude(), a.getLongitude(), toJsString(title)));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private String toJsString(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }

    // ========== Backend HTTP helpers ==========
    private String httpGet(String path) throws IOException {
        URL url = new URL(BASE + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        int rc = conn.getResponseCode();
        InputStream is = (rc >= 200 && rc < 400) ? conn.getInputStream() : conn.getErrorStream();
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
    }

    private String httpPost(String path, String jsonBody) throws IOException {
        URL url = new URL(BASE + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try(OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }
        int rc = conn.getResponseCode();
        InputStream is = (rc >= 200 && rc < 400) ? conn.getInputStream() : conn.getErrorStream();
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
    }

    private String httpPut(String path, String jsonBody) throws IOException {
        URL url = new URL(BASE + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try(OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }
        int rc = conn.getResponseCode();
        InputStream is = (rc >= 200 && rc < 400) ? conn.getInputStream() : conn.getErrorStream();
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
    }

    // ========== load all data from backend ==========
    private void loadAllData() {
        new Thread(() -> {
            try {
                // ambulances
                String ambJson = httpGet("/ambulances");
                Type ambType = new TypeToken<List<Ambulance>>(){}.getType();
                List<Ambulance> ambs = gson.fromJson(ambJson, ambType);
                Platform.runLater(() -> {
                    ambulances.setAll(ambs);
                    ambulanceAssignBox.setItems(ambulances);
                    ambulanceSelector.setItems(ambulances);
                });

                // patients
                String patJson = httpGet("/patients");
                Type ptype = new TypeToken<List<Patient>>(){}.getType();
                List<Patient> pats = gson.fromJson(patJson, ptype);
                Platform.runLater(() -> {
                    patients.setAll(pats);
                });

                // hospitals
                String hospJson = httpGet("/hospitals");
                Type htype = new TypeToken<List<Hospital>>(){}.getType();
                List<Hospital> hos = gson.fromJson(hospJson, htype);
                Platform.runLater(() -> {
                    hospitals.setAll(hos);
                });

                refreshMapMarkers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ========== UI events ==========

    private void assignSelectedAmbulanceToPatient() {
        Patient sel = patientListView.getSelectionModel().getSelectedItem();
        Ambulance amb = ambulanceAssignBox.getSelectionModel().getSelectedItem();
        if (sel == null || amb == null) {
            showAlert("Select a patient and an ambulance first");
            return;
        }
        // send assign request
        new Thread(() -> {
            try {
                String path = String.format("/patients/%d/assign?ambulanceId=%d", sel.getId(), amb.getId());
                String resp = httpPost(path, "{}");
                System.out.println("assign response: " + resp);
                loadAllData();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error assigning ambulance: " + e.getMessage());
            }
        }).start();
    }

    private void registerAmbulance() {
        String driver = driverNameField.getText().trim();
        String vehicle = vehicleField.getText().trim();
        String loc = locationField.getText().trim();
        Double lat = parseDouble(latField.getText());
        Double lon = parseDouble(lonField.getText());
        if (driver.isEmpty()) {
            showAlert("Driver name required");
            return;
        }
        Ambulance a = new Ambulance();
        a.setDriverName(driver);
        a.setVehicleNumber(vehicle);
        a.setLocation(loc);
        a.setLatitude(lat);
        a.setLongitude(lon);
        a.setOnDuty(false);
        a.setBusy(false);
        a.setAvailable(true);

        new Thread(() -> {
            try {
                String json = gson.toJson(a);
                String res = httpPost("/ambulances", json);
                Platform.runLater(() -> {
                    showAlert("Registered ambulance: " + res);
                    clearForm();
                    loadAllData();
                });
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error registering: " + e.getMessage());
            }
        }).start();
    }

    private void onAmbulanceSelected() {
        Ambulance a = ambulanceSelector.getSelectionModel().getSelectedItem();
        if (a == null) return;
        // show assigned patient by scanning patients
        Optional<Patient> assigned = patients.stream().filter(p -> p.getAssignedAmbulance() != null && p.getAssignedAmbulance().getId().equals(a.getId())).findFirst();
        if (assigned.isPresent()) {
            Patient p = assigned.get();
            // switch to controller tab and highlight the patient
            Platform.runLater(() -> {
                tabPane.getSelectionModel().select(0);
                patientListView.getSelectionModel().select(p);
                if (p.getLatitude() != null && p.getLongitude() != null) {
                    webEngine.executeScript(String.format("setView(%f,%f,14)", p.getLatitude(), p.getLongitude()));
                }
            });
        } else {
            // no assigned patient: allow toggling duty
            Platform.runLater(() -> {
                showAlert("This ambulance has no assigned patient. Use Toggle Duty to change on-duty status.");
            });
        }
    }

    private void toggleDuty() {
        Ambulance a = ambulanceSelector.getSelectionModel().getSelectedItem();
        if (a == null) {
            showAlert("Select an ambulance first");
            return;
        }
        boolean newDuty = !a.isOnDuty();
        a.setOnDuty(newDuty);
        // update backend
        new Thread(() -> {
            try {
                String json = gson.toJson(a);
                String resp = httpPut("/ambulances/" + a.getId() + "/status", json);
                System.out.println("toggle result: " + resp);
                loadAllData();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error toggling duty: " + e.getMessage());
            }
        }).start();
    }

    private void clearForm() {
        driverNameField.clear();
        vehicleField.clear();
        locationField.clear();
        latField.clear();
        lonField.clear();
    }

    private Double parseDouble(String s) {
        try { return (s == null || s.trim().isEmpty()) ? null : Double.parseDouble(s.trim()); }
        catch (Exception ex) { return null; }
    }

    private void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
            a.showAndWait();
        });
    }
}
