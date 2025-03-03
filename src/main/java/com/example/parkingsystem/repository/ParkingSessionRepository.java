package com.example.parkingsystem.repository;

import com.example.parkingsystem.entity.ParkingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {
	
    List<ParkingSession> findByLicensePlateAndEndTimeIsNull(String licensePlate);
    
    Optional<ParkingSession> findByLicensePlateAndIsActive(String licensePlate, boolean isActive);
}