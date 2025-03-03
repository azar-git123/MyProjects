package com.example.parkingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Data
public class LicensePlateObservation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(hidden=true)
    private Long id;
	private String licensePlate;
    private String streetName;
    private LocalDateTime observationTime;
}