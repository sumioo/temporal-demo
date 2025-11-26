package com.example.temporal.demo.activities;

import org.springframework.stereotype.Component;

@Component
public class GreetingActivitiesImpl implements GreetingActivities {
  @Override
  public String composeGreeting(String greeting, String name) {
    int sleepSeconds = 1 + (int) (Math.random() * 20);
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

  @Override
  public void cleanupAfterFailure(String details) {
    // 这里是你的清理逻辑
    // 例如：更新数据库状态、向监控系统发送警报、回滚之前的操作等
    System.err.println("--- CLEANUP ACTIVITY TRIGGERED ---");
    System.err.println("Failure Details: " + details);
    System.err.println("--- CLEANUP ACTIVITY COMPLETED ---");
  }
}
