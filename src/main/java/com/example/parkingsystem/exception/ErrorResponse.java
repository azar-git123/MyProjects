package com.example.parkingsystem.exception;

import lombok.Data;

@Data
public class ErrorResponse {
	private final String message;
	private final int status;
}
