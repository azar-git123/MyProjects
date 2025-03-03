package com.example.parkingsystem.controller;

import com.example.parkingsystem.entity.ParkingSession;
import com.example.parkingsystem.service.ParkingSessionService;
import com.example.parkingsystem.service.LicensePlateObservationService;
import com.example.parkingsystem.entity.LicensePlateObservation;

import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parking")
public class ParkingSessionController {
    private static final Logger logger = LoggerFactory.getLogger(ParkingSessionController.class);

    private ParkingSessionService parkingSessionService;

    private LicensePlateObservationService licensePlateObservationService;
    
    public ParkingSessionController(ParkingSessionService parkingSessionService, LicensePlateObservationService licensePlateObservationService) {
    	this.parkingSessionService = parkingSessionService;
    	this.licensePlateObservationService = licensePlateObservationService;
    }

    @Operation(summary = "Start the Parking session", description = "Start the Parking session and returns parking session details.")
    @PostMapping("/start")
    public ParkingSession startSession(@RequestParam String licensePlate, @RequestParam String streetName) {
    	logger.info("Parking session started");
        return parkingSessionService.startSession(licensePlate, streetName);
    }

    @Operation(summary = "End the Parking session", description = "End the Parking session and returns the cost")
    @PostMapping("/end")
    public ParkingSession endSession(@RequestParam String licensePlate) {
    	logger.info("Initiating closing of parking session for {}", licensePlate);
        return parkingSessionService.endSession(licensePlate);
    }
    
    @Operation(summary = "License plate observation", description = "Upload the observed license plate data")
    @PostMapping("/upload-observations")
    public ResponseEntity<String> uploadObservations(@RequestBody List<LicensePlateObservation> observations) {
    	licensePlateObservationService.uploadObservations(observations);
        return ResponseEntity.ok("Observations uploaded.");
    }

}