package com.example.parkingsystem.service.impl;

import com.example.parkingsystem.config.StreetParkingPricing;
import com.example.parkingsystem.entity.ParkingSession;
import com.example.parkingsystem.repository.ParkingSessionRepository;
import com.example.parkingsystem.service.ParkingSessionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class ParkingSessionServiceImpl implements ParkingSessionService {
	
    private static final Logger logger = LoggerFactory.getLogger(ParkingSessionServiceImpl.class);

    private ParkingSessionRepository parkingSessionRepository;

    private StreetParkingPricing streetParkingPricingConfig;
    
    @Value("${parkingsystem.timezone}")
    private String timezone;

    public ParkingSessionServiceImpl(ParkingSessionRepository parkingSessionRepository, StreetParkingPricing streetParkingPricingConfig) {
    	this.parkingSessionRepository = parkingSessionRepository;
        this.streetParkingPricingConfig = streetParkingPricingConfig;
    }

    /*
     * Initiate the parking session
     * @param licensePlate - licenseplate number
     * @param streetName - name of the street, where car is parked
     * @return ParkingSession - parking session object with all details mentioned in @see ParkingSession
     * @throws RuntimeException, if invalid street name
     */
    @Override
	public ParkingSession startSession(String licensePlate, String streetName) {
        ParkingSession session = new ParkingSession();
        session.setLicensePlate(licensePlate);
        if(!getStreetConfig(streetName).isEmpty()) {
            session.setStreetName(streetName);
        }
        session.setStartTime(LocalDateTime.now(ZoneId.of(timezone)));
        session.setActive(true);
       	logger.info("Parking session object with current time : {}", session);
        return parkingSessionRepository.save(session);
    }

    /*
     * End the parking session
     * @param licensePlate - licenseplate number
     * @return ParkingSession - parking session object with all details mentioned in @see ParkingSession
     * @throws RuntimeException, if invalid street name or no active session found for the license plate
     */
    @Override
	public ParkingSession endSession(String licensePlate) {
    	List<ParkingSession> sessions = parkingSessionRepository.findByLicensePlateAndEndTimeIsNull(licensePlate);
        if (sessions.isEmpty()) {
        	logger.error("No session found for : {}", licensePlate);
            throw new RuntimeException("No active session found for license plate: " + licensePlate);
        }
        ParkingSession session = sessions.get(0);
        session.setActive(false);
        session.setEndTime(LocalDateTime.now(ZoneId.of(timezone)));
        logger.info("Parking session for license plate number: {}, is found : {}", session.getLicensePlate(), session);

        // Calculate the cost of parking
        Map<String, Integer> map = getStreetConfig(session.getStreetName());
        double pricePerMinute = map.get(session.getStreetName());
        double secondsParked = ChronoUnit.SECONDS.between(session.getStartTime(), session.getEndTime());
        logger.info("secondsParked is : {}", secondsParked);
        double minutesParked = Math.ceil(secondsParked/60.0);
        logger.info("minutesParked is : {}", minutesParked);
        double cost = minutesParked * pricePerMinute;
        logger.info("Ending parking session with cost for license number {} is {}", session.getLicensePlate(), cost);
        session.setCost(cost);

        return parkingSessionRepository.save(session);
    }
    
    /*
     * Returns the config map to return configured street with pricing details
     * @param streetName - name of the street, where car is parked
     * @return the config map with streetname as key and pricing as value
     * @throws RuntimeException, if invalid street name
     */
    private Map<String, Integer> getStreetConfig(String streetName) {
        Map<String, Integer> map = streetParkingPricingConfig.getValues();
        if(!map.containsKey(streetName)) {
        	logger.error("Invalid street: {}", streetName);
        	throw new RuntimeException("No street found for street name: " + streetName);
        }
        return map;
    }
}