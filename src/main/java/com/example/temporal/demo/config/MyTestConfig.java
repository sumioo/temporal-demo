package com.example.temporal.demo.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyTestConfig {
    
    @Bean 
    public MyENV test() {
        MyENV myTest = new MyENV();
        myTest.envA = 20;
        return myTest;
    }

    @Bean
    public ApplicationRunner testStart(MyENV myTest) {
        return args -> {
            System.out.println("=".repeat(myTest.envA) + "testStart" + "=".repeat(myTest.envA));
        };
    }
}
