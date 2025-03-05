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
import java.time.LocalTime;
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
        session.setStartTime(LocalDateTime.now(ZoneId.of(timezone)).truncatedTo(ChronoUnit.MINUTES));
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
        session.setEndTime(LocalDateTime.now(ZoneId.of(timezone)).truncatedTo(ChronoUnit.MINUTES));
        logger.info("Parking session for license plate number: {}, is found : {}", session.getLicensePlate(), session);

        // Calculate the cost of parking
        double cost = calculateCost(session);
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
    
    /*
     * Returns the cost for a parking session
     * @param session - parking session object @see ParkingSession
     * @return the cost
     */
    private double calculateCost(ParkingSession session) {
        LocalDateTime start = session.getStartTime();
        LocalDateTime end = session.getEndTime();
        Map<String, Integer> map = getStreetConfig(session.getStreetName());
        double pricePerMinute = map.get(session.getStreetName());

        double totalMinutes = 0;
        while (start.isBefore(end)) {
            if (isChargeable(start)) {
                totalMinutes++;
            }
            start = start.plusMinutes(1);
        }
        logger.info("Price per minute is : {}", pricePerMinute);
        logger.info("Total chargeable minutes are : {}", totalMinutes);
        return totalMinutes * pricePerMinute /100.0;
    }
    
    /*
     * Validates whether the time is chargeable or not.No charge for Sunday and between 9PM and 8AM on other days
     * @param time - any time
     * @return true or false
     */
    private boolean isChargeable(LocalDateTime time) {
    	LocalTime localTime = time.toLocalTime();
        return !(localTime.isAfter(LocalTime.of(20, 59)) || localTime.isBefore(LocalTime.of(8, 0)) || time.getDayOfWeek().getValue() == 7);
    }
}
