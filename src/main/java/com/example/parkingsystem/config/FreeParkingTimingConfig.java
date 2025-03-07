package com.example.parkingsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import lombok.Getter;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@Getter
public class FreeParkingTimingConfig {

	private static final Logger logger = LoggerFactory.getLogger(FreeParkingTimingConfig.class);

	@Value("${parkingsystem.freeparking.starttime}")
	private String freeParkingStartTime;

	@Value("${parkingsystem.freeparking.endtime}")
	private String freeParkingEndTime;

	private int freeParkingStartHrs;
	private int freeParkingStartMins;
	private int freeParkingEndHrs;
	private int freeParkingEndMins;

	@PostConstruct
	public void init() {
		try {
			LocalTime freeParkingStart = LocalTime.parse(freeParkingStartTime);
			LocalTime freeParkingEnd = LocalTime.parse(freeParkingEndTime);

			freeParkingStartHrs = freeParkingStart.getHour();
			freeParkingStartMins = freeParkingStart.getMinute();
			logger.info("Free parking start time is : {}:{}", freeParkingStartHrs, freeParkingStartMins);

			freeParkingEndHrs = freeParkingEnd.getHour();
			freeParkingEndMins = freeParkingEnd.getMinute();
			logger.info("Free parking end time is : {}:{}", freeParkingEndHrs, freeParkingEndMins);
		} catch (DateTimeParseException e) {
			logger.error("Invalid free parking start/end time configured: start time : {}, end time : {}", freeParkingStartTime, freeParkingEndTime);
			throw new RuntimeException("Invalid free parking start/end time configured");
		}
	}
}

