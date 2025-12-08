package com.example.temporal.demo.workflow;

import com.example.temporal.demo.activities.GreetingActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GreetingWorkflowImpl implements GreetingWorkflow {
  private static final org.slf4j.Logger log = Workflow.getLogger(MainGreetingWorkflowImpl.class);
  private final GreetingActivities activities = Workflow.newActivityStub(
      GreetingActivities.class,
      ActivityOptions.newBuilder()
          .setStartToCloseTimeout(Duration.ofSeconds(22))
          .setRetryOptions(
              io.temporal.common.RetryOptions.newBuilder()
                  .setMaximumAttempts(1)
                  .build())
          .build());

  @Override
  public String getGreeting(String name) {

    // 0. 先调用 longGreeting 活动
    String longGreeting = activities.longGreeting(3);
    log.warn(longGreeting);

    // 3. 调用 longWait 活动
    // longWait();
    // int randomInt = activities.randomInt();
    // log.warn("randomInt: " + randomInt);

    // String randomError = activities.randomError();
    // log.warn(randomError);

    List<Integer> randInts = randInts();
    log.warn("randInts: " + randInts);

    if(randInts.get(0) > 5) {
      // throw new RuntimeException("test");
      throw ApplicationFailure.newNonRetryableFailure("randInts.get(0) > 5", randInts.get(0) + " > 5");
    }


    // 1. 异步启动 10 个活动
    List<Promise<String>> promises = new ArrayList<>(10);
    for (int i = 0; i < 10; i++) {
      promises.add(Async.function(activities::composeGreeting, "Hello" + i, name));
    }

    // 2. 任意一个完成就实时处理（按索引删除，保证剩余数量递减）
    StringBuilder sb = new StringBuilder();
    for (Promise<String> promise : promises) {
      try {
        sb.append(promise.get());
      } catch (Exception e) {
        log.error("==========Error Greeting: " + e.getClass());
        throw Workflow.wrap(e);
      }
    }
    log.warn(name + "==================================All activities completed.");
    return sb.toString() + name + " All activities completed.";
  }

  void longWait() {
    long start = System.currentTimeMillis();
    while (System.currentTimeMillis() - start < 2000) {
      // 什么都不做，但也不yield
      // 这会触发 PotentialDeadlockException
    }
  }

  List<Integer> randInts() {
    List<Integer> randInts = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      randInts.add((int) (Math.random() * 10));
    }
    return randInts;
  }
}
