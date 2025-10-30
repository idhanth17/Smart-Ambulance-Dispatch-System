package com.smartambulance.frontend.controllers;

import com.smartambulance.frontend.models.Ambulance;
import com.smartambulance.frontend.models.Patient;
import com.smartambulance.frontend.util.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.List;

public class AmbulanceTabController {
    @FXML private ComboBox<Ambulance> existingCombo;
    @FXML private TextField driverNameField;
    @FXML private TextField vehicleNumberField;
    @FXML private TextField locationField;
    @FXML private TextField latField;
    @FXML private TextField lonField;
    @FXML private TabPane detailTabPane;

    @FXML
    public void initialize() {
        refreshExisting();
    }

    @FXML public void onRefresh(){
        refreshExisting();
    }

    private void refreshExisting(){
        Platform.runLater(() -> {
            try {
                List<Ambulance> ambulances = ApiClient.getAmbulances();
                existingCombo.getItems().setAll(ambulances);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML public void onSelectExisting(){
        Ambulance selected = existingCombo.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        // open a new tab with ambulance detail
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ambulance_detail.fxml"));
            Tab t = new Tab("Amb: " + (selected.getDriverName() != null ? selected.getDriverName() : selected.getId()));
            t.setContent(loader.load());
            AmbulanceDetailController ctl = loader.getController();
            ctl.setAmbulance(selected);
            detailTabPane.getTabs().add(t);
            detailTabPane.getSelectionModel().select(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML public void onRegister(){
        try {
            Ambulance a = new Ambulance();
            a.setDriverName(driverNameField.getText());
            a.setVehicleNumber(vehicleNumberField.getText());
            a.setLocation(locationField.getText());
            try {
                a.setLatitude(Double.parseDouble(latField.getText()));
                a.setLongitude(Double.parseDouble(lonField.getText()));
            } catch (Exception ignore) {}
            a.setOnDuty(true);
            a.setBusy(false);

            Ambulance saved = ApiClient.registerAmbulance(a);
            // add to combobox and select
            existingCombo.getItems().add(saved);
            existingCombo.getSelectionModel().select(saved);
            onSelectExisting();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
