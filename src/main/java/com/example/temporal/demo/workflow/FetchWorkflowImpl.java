package com.example.temporal.demo.workflow;

import com.example.temporal.demo.activities.GreetingActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class FetchWorkflowImpl implements FetchWorkflow {

    private final GreetingActivities activities = Workflow.newActivityStub(
            GreetingActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(20))
                    .setRetryOptions(
                            io.temporal.common.RetryOptions.newBuilder()
                                    .setMaximumAttempts(3)
                                    .build())
                    .build());

    @Override
    public String fetchUrls(List<String> urls) {

        // 0. 先调用 longGreeting 活动
        String longGreeting = activities.longGreeting(10);
        System.out.println(longGreeting);

        activities.longGreeting(8);

        activities.longGreeting(5);

        // 这一步已经触发了所有活动的并发执行
        List<Promise<String>> promises = new ArrayList<>();
        for (String url : urls) {
            promises.add(Async.function(activities::fetchURL, url));
        }

        StringBuilder sb = new StringBuilder();
        // 直接按顺序获取结果。
        // 因为任务已经并发运行，这里只是在收集结果。
        // 总耗时仍然等于最慢的那个任务的耗时。
        for (Promise<String> promise : promises) {
            try {
                sb.append(promise.get()).append("; ");
            } catch (Exception e) {
                sb.append("Failed to fetch: ").append(e.getMessage()).append("; ");
            }
        }
        return sb.toString();
    }
}