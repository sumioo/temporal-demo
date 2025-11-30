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

        List<Promise<String>> promises = new ArrayList<>();
        for (String url : urls) {
            promises.add(Async.function(activities::fetchURL, url));
        }

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
                    sb.append(result).append("; ");
                } catch (Exception e) {
                    sb.append("Failed to fetch: ").append(e.getMessage()).append("; ");
                } finally {
                    promises.remove(doneIndex);
                }
            }
        }
        return sb.toString();
    }
}