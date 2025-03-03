package com.example.parkingsystem.controller;

import com.example.parkingsystem.entity.LicensePlateObservation;
import com.example.parkingsystem.entity.ParkingSession;
import com.example.parkingsystem.service.impl.LicensePlateObservationServiceImpl;
import com.example.parkingsystem.service.impl.ParkingSessionServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ParkingSessionControllerTest {
    @Mock
    private ParkingSessionServiceImpl service;
    
    @Mock
    private LicensePlateObservationServiceImpl licensePlateObservationService;

    @InjectMocks
    private ParkingSessionController controller;
    
    private MockMvc mockMvc;

       @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testStartSession() throws Exception {
        ParkingSession session = new ParkingSession();
        session.setLicensePlate("ABC123");
        session.setStreetName("Java");

        when(service.startSession("ABC123", "Java")).thenReturn(session);

        mockMvc.perform(post("/api/parking/start")
                .param("licensePlate", "ABC123")
                .param("streetName", "Java")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("ABC123"))
                .andExpect(jsonPath("$.streetName").value("Java"));

        verify(service, times(1)).startSession("ABC123", "Java");
    }

    @Test
    public void testEndSession() throws Exception {
        ParkingSession session = new ParkingSession();
        session.setLicensePlate("ABC123");
        session.setStreetName("Java");
        session.setEndTime(LocalDateTime.now());
        session.setCost(150); // Example cost

        when(service.endSession("ABC123")).thenReturn(session);

        mockMvc.perform(post("/api/parking/end")
                .param("licensePlate", "ABC123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("ABC123"))
                .andExpect(jsonPath("$.endTime").isNotEmpty())
                .andExpect(jsonPath("$.cost").value(150));

        verify(service, times(1)).endSession("ABC123");
    }
    
    @Test
    public void testUploadObservations() throws Exception {
    	
    	List<LicensePlateObservation> licensePlateObservations = new ArrayList<>();
    	LicensePlateObservation licensePlateObservation = new LicensePlateObservation();
    	licensePlateObservation.setId(1L);
    	licensePlateObservation.setLicensePlate("TN1234");
    	licensePlateObservation.setStreetName("Java");
    	licensePlateObservation.setObservationTime(LocalDateTime.now());
    	licensePlateObservations.add(licensePlateObservation);
    	String licensePlateRequestJson = new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(licensePlateObservations);

        doNothing().when(licensePlateObservationService).uploadObservations(licensePlateObservations);

        mockMvc.perform(post("/api/parking/upload-observations")
                .content(licensePlateRequestJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(licensePlateObservationService, times(1)).uploadObservations(licensePlateObservations);
    }
}
