package com.example.temporal.demo.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface GreetingActivities {
  @ActivityMethod
  String composeGreeting(String greeting, String name);


  @ActivityMethod
  String longGreeting(int n);

  @ActivityMethod
  String randomError();

  @ActivityMethod
  int randomInt();

  @ActivityMethod
  void cleanupAfterFailure(String details);

  @ActivityMethod
  String fetchURL(String url);
}

