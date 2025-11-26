package com.example.temporal.demo.activities;

import org.springframework.stereotype.Component;

@Component
public class GreetingActivitiesImpl implements GreetingActivities {
  @Override
  public String composeGreeting(String greeting, String name) {
    int sleepSeconds = 1 + (int)(Math.random() * 20);
    try {
      Thread.sleep(1000 * sleepSeconds);
      if (sleepSeconds > 10) {
        throw new RuntimeException("sleep seconds > 10");
      }
    } catch (InterruptedException e) {
      System.out.println(e);
    }
    return String.format("%s %s! (sleep %d seconds)", greeting, name, sleepSeconds);
  }
}

