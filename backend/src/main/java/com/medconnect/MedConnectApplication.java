package com.medconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MedConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedConnectApplication.class, args);
    }
}
