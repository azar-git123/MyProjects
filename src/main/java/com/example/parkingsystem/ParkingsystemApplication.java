package com.example.parkingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.parkingsystem.config.StreetParkingPricing;

@SpringBootApplication
@EnableConfigurationProperties(StreetParkingPricing.class)
@EnableScheduling
public class ParkingsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParkingsystemApplication.class, args);
	}

}
