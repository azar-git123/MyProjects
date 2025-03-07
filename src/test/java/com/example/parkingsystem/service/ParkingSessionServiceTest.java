package com.example.parkingsystem.service;

import com.example.parkingsystem.config.HolidayConfig;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingSessionServiceTest {
    @Mock
    private ParkingSessionRepository parkingSessionRepository;
    
    @Mock
    private StreetParkingPricing streetParkingPricingConfig;
    
    @Mock
    private HolidayConfig holidayConfig;

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
        session.setStartTime(LocalDateTime.now(ZoneId.of("Europe/Amsterdam")).truncatedTo(ChronoUnit.MINUTES));
        
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
        session.setStartTime(LocalDateTime.now(ZoneId.of("Europe/Amsterdam")).truncatedTo(ChronoUnit.MINUTES));

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
        session.setStartTime(LocalDateTime.now(ZoneId.of("Europe/Amsterdam")).truncatedTo(ChronoUnit.MINUTES).minusMinutes(3));
        

        ReflectionTestUtils.setField(service, "freeParkingStartTime", "20:59");
        ReflectionTestUtils.setField(service, "freeParkingEndTime", "08:00");

        Map<String, Integer> pricing = new HashMap<>();
        pricing.put("Java", 15);
        when(parkingSessionRepository.findByLicensePlateAndEndTimeIsNull("ABC123")).thenReturn(Collections.singletonList(session));
        when(parkingSessionRepository.save(any(ParkingSession.class))).thenReturn(session);
        when(streetParkingPricingConfig.getValues()).thenReturn(pricing);

        ParkingSession result = service.endSession("ABC123");
        assertNotNull(result);
        assertEquals("ABC123", result.getLicensePlate());
        assertNotNull(result.getEndTime());
        assertEquals(0.45, result.getCost());
    }
    
    @Test
    public void testEndSessionAfterSunday() {
        ParkingSession session = spy(new ParkingSession());
        session.setLicensePlate("ABC123");
        session.setStreetName("Java");
        session.setStartTime(LocalDateTime.of(2025, 3, 1, 20, 0));//1st march 2025 8 P.M - Saturday
        
        ReflectionTestUtils.setField(service, "freeParkingStartTime", "20:59");
        ReflectionTestUtils.setField(service, "freeParkingEndTime", "08:00");

        Map<String, Integer> pricing = new HashMap<>();
        pricing.put("Java", 15);
        when(parkingSessionRepository.findByLicensePlateAndEndTimeIsNull("ABC123")).thenReturn(Collections.singletonList(session));
        when(parkingSessionRepository.save(any(ParkingSession.class))).thenReturn(session);
        when(streetParkingPricingConfig.getValues()).thenReturn(pricing);
        when(session.getEndTime()).thenReturn(LocalDateTime.of(2025, 3, 3, 10, 0));//3rd March 2025 10 A.M - Monday

        ParkingSession result = service.endSession("ABC123");
        assertNotNull(result);
        assertEquals("ABC123", result.getLicensePlate());
        assertNotNull(result.getEndTime());
        assertEquals(27, result.getCost());
    }
    
    @Test
    public void testEndSessionAfterHoliday() {
        ParkingSession session = spy(new ParkingSession());
        session.setLicensePlate("ABC123");
        session.setStreetName("Java");
        session.setStartTime(LocalDateTime.of(2025, 3, 1, 20, 0));//1st march 2025 8 P.M - Saturday
        
        ReflectionTestUtils.setField(service, "freeParkingStartTime", "20:59");
        ReflectionTestUtils.setField(service, "freeParkingEndTime", "08:00");

        Map<String, Integer> pricing = new HashMap<>();
        pricing.put("Java", 15);
        when(parkingSessionRepository.findByLicensePlateAndEndTimeIsNull("ABC123")).thenReturn(Collections.singletonList(session));
        when(parkingSessionRepository.save(any(ParkingSession.class))).thenReturn(session);
        when(streetParkingPricingConfig.getValues()).thenReturn(pricing);
        when(holidayConfig.getHolidays()).thenReturn(List.of(LocalDate.of(2025, 3, 3)));
        when(session.getEndTime()).thenReturn(LocalDateTime.of(2025, 3, 4, 10, 0));//4th March 2025 10 A.M - Monday

        ParkingSession result = service.endSession("ABC123");
        assertNotNull(result);
        assertEquals("ABC123", result.getLicensePlate());
        assertNotNull(result.getEndTime());
        assertEquals(27, result.getCost());
    }
    
    @Test
    public void testEndSessionInvalidateFreeParkingTime() {
        ParkingSession session = new ParkingSession();
        session.setLicensePlate("ABC123");
        session.setStreetName("Java");
        session.setStartTime(LocalDateTime.now(ZoneId.of("Europe/Amsterdam")).truncatedTo(ChronoUnit.MINUTES).minusMinutes(3));
        

        ReflectionTestUtils.setField(service, "freeParkingStartTime", "20:59");
        ReflectionTestUtils.setField(service, "freeParkingEndTime", "8:00");

        Map<String, Integer> pricing = new HashMap<>();
        pricing.put("Java", 15);
        when(parkingSessionRepository.findByLicensePlateAndEndTimeIsNull("ABC123")).thenReturn(Collections.singletonList(session));
        when(streetParkingPricingConfig.getValues()).thenReturn(pricing);

        Exception exception = assertThrows(RuntimeException.class, () -> {
        	service.endSession("ABC123");
        });

        String expectedMessage = "Invalid free parking start/end time configured";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
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
        session.setStartTime(LocalDateTime.now(ZoneId.of("Europe/Amsterdam")).truncatedTo(ChronoUnit.MINUTES));

        when(parkingSessionRepository.findByLicensePlateAndEndTimeIsNull("ABC123")).thenReturn(Collections.singletonList(session));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.endSession("ABC123");
        });

        String expectedMessage = "No street found for street name: Java";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}