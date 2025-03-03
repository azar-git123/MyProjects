package com.example.parkingsystem.service.impl;

import com.example.parkingsystem.entity.LicensePlateObservation;
import com.example.parkingsystem.repository.LicensePlateObservationRepository;
import com.example.parkingsystem.repository.ParkingSessionRepository;
import com.example.parkingsystem.service.LicensePlateObservationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
public class LicensePlateObservationServiceImpl implements LicensePlateObservationService {
	
	private static final Logger logger = LoggerFactory.getLogger(LicensePlateObservationServiceImpl.class);
	
    private LicensePlateObservationRepository licensePlateObservationRepository;
    
    private ParkingSessionRepository parkingSessionRepository;
    
    public LicensePlateObservationServiceImpl(LicensePlateObservationRepository licensePlateObservationRepository, ParkingSessionRepository parkingSessionRepository) {
    	this.licensePlateObservationRepository = licensePlateObservationRepository;
    	this.parkingSessionRepository = parkingSessionRepository;
    }
    
    /*
     * Uploads the license plate data observed during scan
     * @param observations - list of observed license plate data 
     */
    @Override
	public void uploadObservations(List<LicensePlateObservation> observations) {
    	//Clear old data before uploading
    	licensePlateObservationRepository.deleteAll();
    	logger.info("Observed license plates details are : {}", observations);
    	licensePlateObservationRepository.saveAll(observations);
    }

    /*
     * Generate a report of unregistered license plates
     */
    @Override
	@Scheduled(cron = "${parkingsystem.cron.unregistered.plates}")
    public void identifyUnregisteredPlates() {
    	logger.info("Scheduler starting to check for any unregistered plates");
    	List<LicensePlateObservation> unregisteredPlates = licensePlateObservationRepository.findAll().stream()
                .filter(data -> parkingSessionRepository.findByLicensePlateAndIsActive(data.getLicensePlate(), false).isPresent())
                .toList();
    	logger.info("Unregistered plates are : {}", unregisteredPlates);
    	// Generate and write the report to a file
        writeReportToFile(unregisteredPlates);
    }
    
    private void writeReportToFile(List<LicensePlateObservation> unregisteredPlates) {
        File reportFile = new File("unregistered_plates_report.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
            writer.write("Unregistered Plates Report\n");
            writer.write("==========================\n");

            for (LicensePlateObservation data : unregisteredPlates) {
                writer.write(String.format(
                    "License Plate: %s, Street: %s, Date: %s\n",
                    data.getLicensePlate(),
                    data.getStreetName(),
                    data.getObservationTime()
                ));
            }

            logger.info("Report generated: {}", reportFile.getAbsolutePath());
        } catch (IOException e) {
        	logger.error("Failed to write report file: " + e.getMessage());
        }
    }
}