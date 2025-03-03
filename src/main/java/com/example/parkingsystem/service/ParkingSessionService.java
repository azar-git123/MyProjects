package com.example.parkingsystem.service;

import com.example.parkingsystem.entity.ParkingSession;

public interface ParkingSessionService {

	/*
	 * Initiate the parking session
	 * @param licensePlate - licenseplate number
	 * @param streetName - name of the street, where car is parked
	 * @return ParkingSession - parking session object with all details mentioned in @see ParkingSession
	 * @throws RuntimeException, if invalid street name
	 */
	ParkingSession startSession(String licensePlate, String streetName);

	/*
	 * End the parking session
	 * @param licensePlate - licenseplate number
	 * @return ParkingSession - parking session object with all details mentioned in @see ParkingSession
	 * @throws RuntimeException, if invalid street name or no active session found for the license plate
	 */
	ParkingSession endSession(String licensePlate);

}