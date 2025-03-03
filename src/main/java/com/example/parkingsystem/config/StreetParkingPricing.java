package com.example.parkingsystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

import java.util.Map;

@ConfigurationProperties(prefix = "parkingsystem.street")
@Data
public class StreetParkingPricing {

    private Map<String, Integer> values;
}