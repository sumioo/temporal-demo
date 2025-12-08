package com.example.temporal.demo.activities;

import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

@Component
public class GreetingActivitiesImpl implements GreetingActivities {
  @Override
  public String composeGreeting(String greeting, String name) {
    int sleepSeconds = 1 + (int) (Math.random() * 10);
    try {
      Thread.sleep(1000 * sleepSeconds);
      if (sleepSeconds > 5) {
        throw new RuntimeException("sleep seconds > 5");
      }
    } catch (InterruptedException e) {
      System.out.println(e);
    }
    if (sleepSeconds > 5) {
      return null;
    }
    return String.format("%s %s! (sleep %d seconds)", greeting, name, sleepSeconds);
  }

  @Override
  public String longGreeting(int n) {
    try {
      Thread.sleep(1000 * n);
    } catch (InterruptedException e) {
      System.out.println(e);
    }
    return "longGreeting done";
  }

  @Override
  public String fetchURL(String url)  {
    try {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(url))
              .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      String body = response.body();
      String truncatedBody = body.substring(0, Math.min(body.length(), 100));
      System.out.println("Response body (first 100 bytes): " + truncatedBody);
      return "Response status code: " + response.statusCode() + ", body: " + truncatedBody;
    } catch (Exception e) {
      System.err.println("Failed to fetch URL: " + e.getMessage());
      throw new IllegalStateException("Failed to fetch URL", e);
    }
  }

  @Override
  public void cleanupAfterFailure(String details) {
    // 这里是你的清理逻辑
    // 例如：更新数据库状态、向监控系统发送警报、回滚之前的操作等
    System.err.println("--- CLEANUP ACTIVITY TRIGGERED ---");
    System.err.println("Failure Details: " + details);
    System.err.println("--- CLEANUP ACTIVITY COMPLETED ---");
  }

  @Override
  public String randomError() {
    int n = (int) (Math.random() * 10);
    if(n == 2) {
      throw new NullPointerException("random error null");
    }
    if (n == 3) {
      throw new IllegalArgumentException("random error illegal");
    }
    if (n == 4 || n == 5) {
      throw new RuntimeException("random error runtime");
    }
    return "no error";
  }

  @Override
  public int randomInt() {
    int n = (int) (Math.random() * 10);
    return n;
  }
}
