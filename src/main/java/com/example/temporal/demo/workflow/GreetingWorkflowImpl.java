package com.example.temporal.demo.workflow;

import com.example.temporal.demo.activities.GreetingActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import java.util.Iterator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GreetingWorkflowImpl implements GreetingWorkflow {
  private final GreetingActivities activities = Workflow.newActivityStub(
      GreetingActivities.class,
      ActivityOptions.newBuilder()
          .setStartToCloseTimeout(Duration.ofSeconds(22))
          .build());

  @Override
  public String getGreeting(String name) {
    // 1. 异步启动 10 个活动
    List<Promise<String>> promises = new ArrayList<>(10);
    for (int i = 0; i < 10; i++) {
      promises.add(Async.function(activities::composeGreeting, "Hello" + i, name));
    }

    // 2. 任意一个完成就实时处理（按索引删除，保证剩余数量递减）
    StringBuilder sb = new StringBuilder();
    while (!promises.isEmpty()) {
      // 先找到已完成的那个 Promise 的索引
      int doneIndex = -1;
      for (int i = 0; i < promises.size(); i++) {
        if (promises.get(i).isCompleted()) {   // 关键 API：isCompleted()
          doneIndex = i;
          break;
        }
      }
      if (doneIndex == -1) {          // 理论上不会，保险
        doneIndex = 0;
      }
      try {
        String result = promises.get(doneIndex).get();
        sb.append(result).append("; ");
        System.out.println("anyOf: " + result);
      } catch (Exception e) {
        System.out.println("anyOf: " + e);
      }
      promises.remove(doneIndex);     // 按索引删，一定成功
      System.out.println("remaining = " + promises.size());
    }
    return sb.toString();
  }
}
