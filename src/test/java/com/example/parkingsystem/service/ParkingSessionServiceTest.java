package com.example.parkingsystem.service;

import com.example.parkingsystem.config.StreetParkingPricing;
import com.example.parkingsystem.entity.ParkingSession;
import com.example.parkingsystem.repository.ParkingSessionRepository;
import com.example.parkingsystem.service.impl.ParkingSessionServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingSessionServiceTest {
    @Mock
    private ParkingSessionRepository parkingSessionRepository;
    
    @Mock
    private StreetParkingPricing streetParkingPricingConfig;

    @InjectMocks
    private ParkingSessionServiceImpl service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(service, "timezone", "Europe/Amsterdam");
    }

    @Test
    public void testStartSession() {
        ParkingSession session = new ParkingSession();
        session.setLicensePlate("ABC123");
        session.setStreetName("Java");
        session.setStartTime(LocalDateTime.now(ZoneId.of("Europe/Amsterdam")));
        
        Map<String, Integer> pricing = new HashMap<>();
        pricing.put("Java", 15);
        
        when(parkingSessionRepository.save(any(ParkingSession.class))).thenReturn(session);
        when(streetParkingPricingConfig.getValues()).thenReturn(pricing);

        ParkingSession result = service.startSession("ABC123", "Java");
        assertNotNull(result);
        assertEquals("ABC123", result.getLicensePlate());
        assertEquals("Java", result.getStreetName());
    }
    
    @Test
    public void testStartSessionStreetNotFound() {
    	ParkingSession session = new ParkingSession();
        session.setLicensePlate("TN1234");
        session.setStreetName("Jakarta");
        session.setStartTime(LocalDateTime.now(ZoneId.of("Europe/Amsterdam")));

        Exception exception = assertThrows(RuntimeException.class, () -> {
        	service.startSession("TN1234", "Jakarta");
        });

        String expectedMessage = "No street found for street name: Jakarta";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testEndSession() {
        ParkingSession session = new ParkingSession();
        session.setLicensePlate("ABC123");
        session.setStreetName("Java");
        session.setStartTime(LocalDateTime.now(ZoneId.of("Europe/Amsterdam")).minusSeconds(50));

        Map<String, Integer> pricing = new HashMap<>();
        pricing.put("Java", 15);
        when(parkingSessionRepository.findByLicensePlateAndEndTimeIsNull("ABC123")).thenReturn(Collections.singletonList(session));
        when(parkingSessionRepository.save(any(ParkingSession.class))).thenReturn(session);
        when(streetParkingPricingConfig.getValues()).thenReturn(pricing);

        ParkingSession result = service.endSession("ABC123");
        assertNotNull(result);
        assertEquals("ABC123", result.getLicensePlate());
        assertNotNull(result.getEndTime());
        assertEquals(15, result.getCost());
    }

    @Test
    public void testEndSessionNoActiveSession() {
        when(parkingSessionRepository.findByLicensePlateAndEndTimeIsNull("ABC123")).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.endSession("ABC123");
        });

        String expectedMessage = "No active session found for license plate: ABC123";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testEndSessionStreetNotFound() {
        ParkingSession session = new ParkingSession();
        session.setLicensePlate("ABC123");
        session.setStreetName("Java");
        session.setStartTime(LocalDateTime.now());

        when(parkingSessionRepository.findByLicensePlateAndEndTimeIsNull("ABC123")).thenReturn(Collections.singletonList(session));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.endSession("ABC123");
        });

        String expectedMessage = "No street found for street name: Java";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}