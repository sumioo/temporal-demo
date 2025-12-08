package com.example.temporal.demo.workflow;

import com.example.temporal.demo.activities.GreetingActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.failure.ActivityFailure;
import io.temporal.workflow.CancellationScope;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GreetingWorkflowImpl2 implements GreetingWorkflow {
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
    List<Promise<String>> promises = new ArrayList<>(10);

    // 1. 创建一个可取消的作用域，用于包裹所有的 Activity
    CancellationScope allActivitiesScope = Workflow.newCancellationScope(() -> {
        for (int i = 0; i < 10; i++) {
            promises.add(Async.function(activities::composeGreeting, "Hello" + i, name));
        }
    });
    allActivitiesScope.run(); // .run() 会立即返回，因为内部都是 Async 调用

    StringBuilder sb = new StringBuilder();
    while (!promises.isEmpty()) {
        Workflow.await(() -> promises.stream().anyMatch(Promise::isCompleted));

        int doneIndex = -1;
        for (int i = 0; i < promises.size(); i++) {
            if (promises.get(i).isCompleted()) {
                doneIndex = i;
                break;
            }
        }

        if (doneIndex != -1) {
            Promise<String> completedPromise = promises.get(doneIndex);
            try {
                String result = completedPromise.get();
                System.out.println("anyOf: " + result);
                sb.append(result).append("; ");

            } catch (ActivityFailure e) {
                Workflow.getLogger(GreetingWorkflowImpl.class).error("Activity failed. Initiating cancellation and cleanup.", e);

                // 取消作用域内的所有其他活动
                allActivitiesScope.cancel();

                // 在一个分离的作用域中执行清理，确保它不会被取消
                Workflow.newDetachedCancellationScope(() -> {
                    activities.cleanupAfterFailure(
                        "Activity failed, cancelling remaining tasks. Cause: " + e.getCause().getMessage());
                }).run();

                return "WORKFLOW FAILED: An activity failed and remaining tasks were cancelled.";

            } finally {
                promises.remove(doneIndex);
            }
        }
    }

    return sb.toString();
  }
}
