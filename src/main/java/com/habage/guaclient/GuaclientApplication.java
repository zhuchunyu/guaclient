package com.habage.guaclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@ServletComponentScan
public class GuaclientApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuaclientApplication.class, args);
    }

}

