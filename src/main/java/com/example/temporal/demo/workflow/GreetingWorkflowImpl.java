package com.example.temporal.demo.workflow;

import com.example.temporal.demo.activities.GreetingActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.failure.ActivityFailure;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GreetingWorkflowImpl implements GreetingWorkflow {
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
    // 1. 异步启动 10 个活动
    List<Promise<String>> promises = new ArrayList<>(10);
    for (int i = 0; i < 10; i++) {
      promises.add(Async.function(activities::composeGreeting, "Hello" + i, name));
    }

    // 2. 任意一个完成就实时处理（按索引删除，保证剩余数量递减）
    StringBuilder sb = new StringBuilder();
    while (!promises.isEmpty()) {
      // 使用 Workflow.await() 来安全地等待，直到至少有一个 Promise 完成
      Workflow.await(() -> promises.stream().anyMatch(Promise::isCompleted));

      // 现在，从列表中找到那个已经完成的 Promise 的索引
      int doneIndex = -1;
      for (int i = 0; i < promises.size(); i++) {
        if (promises.get(i).isCompleted()) {
          doneIndex = i;
          break;
        }
      }

      // 如果找到了（理论上总能找到）
      if (doneIndex != -1) {
        Promise<String> completedPromise = promises.get(doneIndex);
        try {
            // 异常会在调用 .get() 时被抛出
            String result = completedPromise.get();
            System.out.println("anyOf: " + result);
            sb.append(result).append("; ");

        } catch (ActivityFailure e) {
            // Activity 在所有重试后最终失败了！
            Workflow.getLogger(GreetingWorkflowImpl.class).error("Activity failed after all retries.", e);

            // 在这里调用清理 Activity
            activities.cleanupAfterFailure("Failed to process greeting. Cause: " + e.getCause().getMessage());

            // 在最终结果中记录一个错误标记
            return sb.append("FAILED_ACTIVITY_CLEANED_UP; ").toString();

        } finally {
            // 无论成功还是失败，都必须将 Promise 移除，以防止死循环
            promises.remove(doneIndex);
            System.out.println("remaining = " + promises.size());
        }
      }
    }

    return sb.toString();
  }
}
