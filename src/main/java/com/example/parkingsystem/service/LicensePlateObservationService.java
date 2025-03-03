package com.example.parkingsystem.service;

import java.util.List;

import com.example.parkingsystem.entity.LicensePlateObservation;

public interface LicensePlateObservationService {

	/*
	 * Uploads the license plate data observed during scan
	 * @param observations - list of observed license plate data 
	 */
	void uploadObservations(List<LicensePlateObservation> observations);

	/*
	 * Generate a report of unregistered license plates
	 */
	void identifyUnregisteredPlates();

}