package com.rentoss.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan("com.rentoss.core")
@SpringBootApplication(scanBasePackages = "com.rentoss")
public class UserApplication {

    public static void main(String[] args){
        SpringApplication.run(UserApplication.class, args);
    }
}
