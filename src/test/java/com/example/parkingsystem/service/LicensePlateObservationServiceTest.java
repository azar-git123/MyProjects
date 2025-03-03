package com.example.parkingsystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.parkingsystem.entity.LicensePlateObservation;
import com.example.parkingsystem.entity.ParkingSession;
import com.example.parkingsystem.repository.LicensePlateObservationRepository;
import com.example.parkingsystem.repository.ParkingSessionRepository;
import com.example.parkingsystem.service.impl.LicensePlateObservationServiceImpl;

@ExtendWith(MockitoExtension.class)
public class LicensePlateObservationServiceTest {
	
	@Mock
    private LicensePlateObservationRepository licensePlateObservationRepository;
    
	@Mock
    private ParkingSessionRepository parkingSessionRepository;

    @InjectMocks
    private LicensePlateObservationServiceImpl service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testIdentifyUnregisteredPlates() {
    	
    	LocalDateTime currentTime = LocalDateTime.now(ZoneId.of("Europe/Amsterdam"));
    	List<LicensePlateObservation> observations = new ArrayList<>();
    	LicensePlateObservation licensePlateObservation = new LicensePlateObservation();
    	licensePlateObservation.setLicensePlate("TN5678");
    	licensePlateObservation.setStreetName("Jakarta");
    	licensePlateObservation.setObservationTime(currentTime);
    	observations.add(licensePlateObservation);

        ParkingSession session = new ParkingSession();
        session.setLicensePlate("TN5678");
        session.setStreetName("Jakarta");
        session.setStartTime(currentTime.minusSeconds(420));
        session.setEndTime(currentTime.minusSeconds(300));
        session.setActive(false);
    	
        Optional<ParkingSession> optionalSession = Optional.of(session);
    	
    	when(licensePlateObservationRepository.findAll()).thenReturn(observations);
    	when(parkingSessionRepository.findByLicensePlateAndIsActive(licensePlateObservation.getLicensePlate(), false)).thenReturn(optionalSession);
        service.identifyUnregisteredPlates();
        assertThat(new File("unregistered_plates_report.txt")).isNotEmpty();
        assertThat(new File("unregistered_plates_report.txt")).hasContent("Unregistered Plates Report\n==========================\nLicense Plate: TN5678, Street: Jakarta, Date: " + currentTime);
    }
    
    @Test
    public void testUploadObservations() {
    	List<LicensePlateObservation> observations = new ArrayList<>();
    	LicensePlateObservation licensePlateObservation = new LicensePlateObservation();
    	licensePlateObservation.setLicensePlate("TN1234");
    	licensePlateObservation.setStreetName("Java");
    	licensePlateObservation.setObservationTime(LocalDateTime.now());
    	observations.add(licensePlateObservation);

        service.uploadObservations(observations);
        verify(licensePlateObservationRepository, times(1)).deleteAll();
        verify(licensePlateObservationRepository, times(1)).saveAll(observations);
    }
}