package com.example.temporal.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TemporalDemoApplication {
  public static void main(String[] args) {
    SpringApplication.run(TemporalDemoApplication.class, args);
  }
}

