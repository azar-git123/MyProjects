package com.example.parkingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class ParkingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	private String licensePlate;
    private String streetName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double cost;
    private boolean isActive;
}