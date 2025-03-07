package com.example.parkingsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Getter
public class HolidayConfig {

    @Value("${parkingsystem.holidays}")
    private String holidays;

    private List<LocalDate> holidayList;

    @PostConstruct
    public void init() {
        holidayList = Arrays.stream(holidays.split(","))
                            .map(LocalDate::parse)
                            .collect(Collectors.toList());
    }
}
