package com.smartambulance.frontend.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartambulance.frontend.models.Ambulance;
import com.smartambulance.frontend.models.Patient;
import com.smartambulance.frontend.models.Hospital;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * ApiClient — handles REST API communication between frontend and backend.
 */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";  // Backend URL
    private static final ObjectMapper mapper = new ObjectMapper();

    // 🔹 GET request helper
    private static String sendGet(String endpoint) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("GET request failed: " + conn.getResponseCode());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();
        return response.toString();
    }

    // 🔹 POST request helper
    private static String sendPost(String endpoint, Object payload) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String json = mapper.writeValueAsString(payload);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        if (conn.getResponseCode() != 200 && conn.getResponseCode() != 201) {
            throw new RuntimeException("POST failed: " + conn.getResponseCode());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();
        return response.toString();
    }

    // 🔹 PUT request helper
    public static void sendPut(String endpoint, Object payload) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String json = mapper.writeValueAsString(payload);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("PUT failed: " + conn.getResponseCode());
        }
    }

    // ✅ Fetch all ambulances
    public static List<Ambulance> getAmbulances() throws Exception {
        String json = sendGet("/ambulances");
        return mapper.readValue(json, new TypeReference<>() {});
    }

    // ✅ Fetch all patients
    public static List<Patient> getPatients() throws Exception {
        String json = sendGet("/patients");
        return mapper.readValue(json, new TypeReference<>() {});
    }

    // ✅ Fetch all hospitals
    public static List<Hospital> getHospitals() throws Exception {
        String json = sendGet("/hospitals");
        return mapper.readValue(json, new TypeReference<>() {});
    }

    // ✅ Assign ambulance to patient
    public static boolean assignAmbulance(Long patientId, Long ambulanceId) throws Exception {
        String endpoint = "/assign";
        String payload = String.format("{\"patientId\": %d, \"ambulanceId\": %d}", patientId, ambulanceId);
        String response = sendPost(endpoint, mapper.readTree(payload));
        return response.contains("success");
    }

    // ✅ Confirm hospital drop
    public static boolean confirmDrop(Long ambulanceId, Long hospitalId) throws Exception {
        String endpoint = "/drop";
        String payload = String.format("{\"ambulanceId\": %d, \"hospitalId\": %d}", ambulanceId, hospitalId);
        String response = sendPost(endpoint, mapper.readTree(payload));
        return response.contains("success");
    }

    // ✅ Register new ambulance driver
    public static Ambulance registerAmbulance(Ambulance ambulance) throws Exception {
        String json = sendPost("/ambulances", ambulance);
        return mapper.readValue(json, Ambulance.class);
    }
}
