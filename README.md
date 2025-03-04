# Parking System

## Overview
This standalone spring boot project contains REST apis for car parking system

## Features
- Users should be able to register and unregister their cars
- The city's parking administrator should be able to put a list of collected license plates data (number, street name and date of observation) in the system  
- A scheduled task to identify the unregistered plates, resulting in the generation of a report, including car plate, date of observation and street name.

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.9.9
- Spring boot 3.4.3

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/azar-git123/MyProjects.git

### Run instructions
1. Open a gitbash terminal and Ensure you're in the root directory of your Spring Boot project.

2. Install maven and then Build the project (if you haven't already) using below command
   ```bash 
   mvn clean install

3. Run the application by Using the following command to start your Spring Boot application:
   ```bash 
   mvn spring-boot:run

4. Access the swagger UI of the application via browser to test the REST apis
   ```bash 
   http://localhost:8080/swagger-ui/index.html

5. Run the jacoco report to view the code coverage for the parking system REST apis
   ```bash
   mvn clean test jacoco:report
