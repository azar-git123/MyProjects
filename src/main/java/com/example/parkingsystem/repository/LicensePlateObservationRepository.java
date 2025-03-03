package com.example.parkingsystem.repository;

import com.example.parkingsystem.entity.LicensePlateObservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicensePlateObservationRepository extends JpaRepository<LicensePlateObservation, Long> {
}